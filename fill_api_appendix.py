#!/usr/bin/env python3
"""
fill_api_appendix.py

Reads every source file under a directory and formats each one as an HTML
section in the style of the build.101 specification files:

    <h3 id="api-<slug>">A.N <code>relative/path/File.java</code></h3>

    <pre><code>...escaped file contents...</code></pre>

The combined blocks are then injected into the element with id
"api-generated" inside one or more existing HTML files (the in-dev specs).

The build.101 files are treated purely as the style reference; the source
of truth is the directory of files this script is pointed at.

Standard library only.

Usage:
    python fill_api_appendix.py <source_dir> <target1.html> [<target2.html> ...]

Example:
    python fill_api_appendix.py \\
        curved-spacetime-loader-module/src/main/java \\
        module-system.html engine.html
"""

from html.parser import HTMLParser
from pathlib import Path
import html as html_module
import re
import sys


# ----------------------------------------------------------------------
# Source extraction and formatting
# ----------------------------------------------------------------------

def iter_source_files(source_dir):
    """Yield files under source_dir in a stable order.

    Top-level files come first (so module-info.java leads the appendix),
    then files in subdirectories sorted by their relative path.
    """
    top = sorted(p for p in source_dir.iterdir() if p.is_file())
    nested = sorted(
        (p for p in source_dir.rglob("*")
         if p.is_file() and p.parent != source_dir),
        key=lambda p: p.relative_to(source_dir).as_posix(),
    )
    yield from top
    yield from nested


def slugify_id(path):
    """Reproduce the id scheme used by the build.101 headings.

    e.g. 'module-info.java'      -> 'api-moduleinfo'
         'CurvedSpacetimeLoader.java' -> 'api-CurvedSpacetimeLoader'
    """
    return "api-" + re.sub(r"[^A-Za-z0-9]", "", path.stem)


def read_text(path):
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return path.read_text(encoding="latin-1", errors="ignore")


def format_appendix_blocks(source_dir):
    """Return a list of *unindented* HTML block strings.

    Each block is a single string that may contain embedded newlines.
    Callers indent only the first line of each block, so the content of
    <pre><code> stays byte-for-byte what the source file contained.
    """
    blocks = []
    for index, filepath in enumerate(iter_source_files(source_dir), start=1):
        rel = filepath.relative_to(source_dir).as_posix()
        content = read_text(filepath).rstrip("\n")
        slug = slugify_id(filepath)

        heading = (
            f'<h3 id="{slug}">A.{index} '
            f'<code>{html_module.escape(rel)}</code></h3>'
        )
        # quote=False: only & < > need escaping inside element content,
        # and build.101 leaves " and ' alone.
        code_block = (
            f"<pre><code>{html_module.escape(content, quote=False)}"
            f"</code></pre>"
        )
        blocks.append(heading)
        blocks.append(code_block)
    return blocks


# ----------------------------------------------------------------------
# Minimal HTML range-finder (stdlib only)
# ----------------------------------------------------------------------

def _find_tag_end(source, start):
    """Return the index of the '>' that closes the tag beginning at `start`.

    Respects quoted attribute values, so <x id="api-generated" data-y="a>b">
    is handled correctly.
    """
    i = start
    quote = None
    while i < len(source):
        c = source[i]
        if quote is not None:
            if c == quote:
                quote = None
        elif c in ('"', "'"):
            quote = c
        elif c == ">":
            return i
        i += 1
    return -1


class _IdRangeFinder(HTMLParser):
    """Locate the first element whose id matches, and record its inner range.

    content_start is the offset immediately after the opening tag's '>'.
    content_end   is the offset of the '<' of the matching closing tag.
    """

    def __init__(self, source, tag_id):
        super().__init__(convert_charrefs=False)
        self._source = source
        self._tag_id = tag_id
        self._line_starts = [0]
        for idx, ch in enumerate(source):
            if ch == "\n":
                self._line_starts.append(idx + 1)
        self.target_tag = None
        self.content_start = None
        self.content_end = None
        self._depth = 0

    def _abs_pos(self):
        line, col = self.getpos()
        return self._line_starts[line - 1] + col

    def _has_target_id(self, attrs):
        return any(k == "id" and v == self._tag_id for k, v in attrs)

    def handle_starttag(self, tag, attrs):
        if self.content_end is not None:
            return
        if self.content_start is not None:
            if tag == self.target_tag:
                self._depth += 1
            return
        if self._has_target_id(attrs):
            start = self._abs_pos()
            end = _find_tag_end(self._source, start)
            if end == -1:
                return
            self.target_tag = tag
            self.content_start = end + 1
            self._depth = 1

    def handle_startendtag(self, tag, attrs):
        if self.content_start is not None or self.content_end is not None:
            return
        if self._has_target_id(attrs):
            start = self._abs_pos()
            end = _find_tag_end(self._source, start)
            if end == -1:
                return
            self.target_tag = tag
            self.content_start = end + 1
            self.content_end = end + 1

    def handle_endtag(self, tag):
        if self.content_start is None or self.content_end is not None:
            return
        if tag == self.target_tag:
            self._depth -= 1
            if self._depth == 0:
                self.content_end = self._abs_pos()


# ----------------------------------------------------------------------
# Injection
# ----------------------------------------------------------------------

def inject_into_html(target_html, blocks, tag_id="api-generated"):
    if not target_html.is_file():
        sys.exit(f"Error: HTML file not found: {target_html}")

    source = target_html.read_text(encoding="utf-8")

    finder = _IdRangeFinder(source, tag_id)
    finder.feed(source)
    finder.close()

    if finder.content_start is None:
        sys.exit(f"Error: no element with id='{tag_id}' found in {target_html}")

    content_end = finder.content_end if finder.content_end is not None else len(source)
    if content_end < finder.content_start:
        content_end = finder.content_start

    # Work out the indent of the line holding the opening tag, so the
    # injected content nests neatly inside it.
    line_start = source.rfind("\n", 0, finder.content_start) + 1
    base_indent = re.match(r"[ \t]*", source[line_start:]).group(0)
    inner_indent = base_indent + "    "

    # Each block's *first* line is indented by inner_indent. Subsequent
    # lines of a multi-line block (i.e. the body of a <pre><code>) are
    # left alone, which keeps the preformatted content flush-left and
    # renders correctly.
    body = "\n\n".join(inner_indent + b for b in blocks)

    # Splice in: newline, body, newline, base indent. The closing tag
    # that follows is then on its own line at the original indent.
    new_content = "\n" + body + "\n" + base_indent

    updated = source[: finder.content_start] + new_content + source[content_end:]
    target_html.write_text(updated, encoding="utf-8")
    print(
        f"Injected {len(blocks)} block(s) into "
        f"<{finder.target_tag} id='{tag_id}'> in {target_html}"
    )


# ----------------------------------------------------------------------
# Main
# ----------------------------------------------------------------------

def main(source_dir, html_files, tag_id="api-generated"):
    src = Path(source_dir)
    if not src.is_dir():
        sys.exit(f"Error: '{source_dir}' is not a directory.")

    blocks = format_appendix_blocks(src)

    for html_path in html_files:
        inject_into_html(Path(html_path), blocks, tag_id=tag_id)


if __name__ == "__main__":
    if len(sys.argv) < 3:
        sys.exit(
            "Usage: fill_api_appendix.py <source_dir> "
            "<target1.html> [<target2.html> ...]"
        )
    main(sys.argv[1], sys.argv[2:])