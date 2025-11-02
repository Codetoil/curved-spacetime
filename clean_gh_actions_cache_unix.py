import json
import subprocess
import sys

while True:
    caches_json_s = subprocess.run(['sh',
                                    '-c',
                                    'gh api '
                                    '-H "Accept: application/vnd.github+json" '
                                    '-H "X-GitHub-Api-Version: 2022-11-28" '
                                    '/repos/Codetoil/curved-spacetime/actions/caches'],
                                   capture_output=True,
                                   encoding='UTF-8').stdout
    print(caches_json_s)

    caches_json = json.loads(caches_json_s)
    print(caches_json)

    cache_ids = [cache['id'] for cache in caches_json['actions_caches']]
    print(cache_ids)
    if len(cache_ids) == 0:
        break

    [print(subprocess.run(['sh',
                           '-c',
                           'gh api '
                           '--method DELETE '
                           '-H "Accept: application/vnd.github+json" '
                           '-H "X-GitHub-Api-Version: 2022-11-28" '
                           '/repos/Codetoil/curved-spacetime/actions/caches/' +
                           str(id)],
                          capture_output=True,
                          encoding='UTF-8')) for id in cache_ids]
