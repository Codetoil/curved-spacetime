> ## Status: exploratory draft — not normative
>
> **This is not a specification in the sense that [`module-system.html`](module-system.html) is
> one.** That document is normative: it states requirements implementations must meet, and where
> the code disagrees with it, the code is wrong. This one is a design exploration. It is here so
> that the architecture built around it — the module system, the observer-driven renderer, the
> loader abstraction — is legible as deliberate rather than accidental.
>
> Concretely:
>
> - **Nothing here is implemented.** `Scene` is an empty class, and §15's step 1 has not started.
> - **Parts are actively disputed by the author.** Revision 3 settles precision — §17.6 makes it a
>   uniform declared input — but *certified error bounds* remain unaddressed: §13 measures
>   residuals and §14.1 falls back to shadowing, and neither produces a number you could rely on.
> - **§16 lists seven open problems**, including whether the coupled field-plus-mesh system is
>   well-posed at all. §15 marks its own steps 11–13 as research-grade.
> - **It straddles two Cartan nodes** (§11.4) and the dispatch differs between them.
>
> It is snapshotted per release alongside the normative specification, so
> `specs/<release>/simulation.md` records what the draft looked like at that build. It is not
> version-stamped internally, though: the revision number below is the document's own, and
> advances independently of the project's release tags.

# Structure-Preserving Adaptive Spacetime Simulation

**Observer-adapted, variationally consistent field evolution with a smooth dynamical resolution field, over a declared Klein pair with per-diamond reduction.**

*Revision 4. Changes from Revision 1 are summarised in Appendix A; from Revision 2 in Appendix B; from Revision 3 in Appendix C.*

---

## 0. Conventions and scope

Signature $(-,+,\ldots,+)$; SI units throughout. Greek indices run $0\ldots d$ with $x^0 = ct$; Latin $i,j$ are spatial on a slice; $a,b$ index target-space or internal directions; $A,B$ index $\mathfrak g$; $K$ indexes cells; $D$ indexes causal diamonds.

**Dimension is declared, not assumed.** Worked figures throughout are quoted at $d+1 = 3+1$ because that is the intended application, but nothing in the construction fixes it; §2.6 states what varies with $d$ and what does not.

Four-velocities are normalised $u\cdot u = -c^2$ unless carrying a hat, in which case $\hat u\cdot\hat u = -1$. **This normalisation is load-bearing** — see §4.1.

Most referenced literature uses other conventions and this is flagged where it matters. Geometric-integration sources (Hairer–Lubich–Wanner, Marsden–West) are signature-free and dimensionless. Numerical-relativity sources (Baumgarte–Shapiro, Alcubierre) use $(-,+,+,+)$ but geometrised units $G = c = 1$; restoring SI reinstates $c^4/G \approx 1.2\times10^{44}\,\mathrm{N}$ in front of curvature terms and $c^2$ factors in the constraint equations. Cartan-geometric sources (Sharpe) fix no signature; MacDowell–Mansouri and Stelle–West run mostly-plus, so their $\eta_{ab}$ and $[\mathfrak z,\mathfrak z]$ signs flip on import.

**SI is the right register for stating results and the wrong one for computing:** implement in code units scaled to the problem, since $c^4/G$ in double precision wrecks conditioning. Convert on output. This is a workaround for 53-bit mantissas specifically; §17.6 makes precision a declared input, and at sufficient width computing directly in SI becomes admissible and the conversion step disappears.

**Scope.** Classical field theory on a manifold carrying a Cartan geometry of declared type $(G,H)$, with matter, optionally with dynamical gravity, evolved so that:

- the discrete evolution is multisymplectic and momentum-map preserving;
- spatial and temporal resolution vary smoothly over spacetime, driven by a single dynamical field;
- the internal reduction $H$ may vary from region to region, chosen for characteristic structure rather than storage;
- resolution is set jointly by observer acuity and by physical validity constraints;
- the scheme is asynchronous and parallel, with conservation stated on causal diamonds rather than global slices.

**Interactive operation** — a human moving an observer and editing the state while the simulation runs — is in scope, and is governed by §17. It is not merely a presentation concern: by §6.2 the observer enters the action, so the manner in which it is controlled decides whether the system remains a gauge theory of $\mathfrak g$ or silently becomes a modified one.

**Out of scope.** Quantum fields, dissipative closures beyond the GENERIC reservoir of §10, and any claim of trajectory fidelity over chaotic timescales (§14).

---

## 1. Design principle

> **Adaptivity destroys structure exactly when the adaptive choice depends on bookkeeping rather than on the state.**

If the step size, mesh motion, and refinement level are smooth functions on phase space, the evolution map remains a symplectomorphism and backward error analysis applies. If any of them depends on a step counter, an error estimator, or a discontinuous level index, the shadow Hamiltonian ceases to exist and energy drifts secularly. This is the content of the Calvo–Sanz-Serna obstruction, and it is the single constraint from which the rest of the design follows.

The corresponding constructive move, used four times:

| Knob | Made dynamical by | Its equation of motion is |
|---|---|---|
| Lapse $N$ | varying node times $t_k$ | energy conservation |
| Shift $N^i$ | varying node positions $x_i$ | momentum conservation |
| Resolution | mesh map $\chi$ with inertia | equidistribution of the monitor |
| Frame choice | section $\zeta$ of the reduction bundle, stepped geodesically | §5.3 |

Ge–Marsden bounds what is achievable: a symplectic, exactly energy-conserving, adaptive integrator on a system with no invariants independent of $H$ *is* the exact flow up to time reparametrisation. The escape is that the reparametrisation is gauge — it is the lapse. The target is therefore not exact energy but **exact structure with bounded energy**: symplectic form exact, momentum maps exact, topological charges exactly integer-valued, energy oscillating in a band of width $O(h^p)$ with zero secular drift.

**Extension to the reduction.** The same criterion decides how $H$ may vary. A *continuous* choice within a conjugacy class — the section field of §5.3 — is a smooth function on phase space and is admissible. A *discrete* choice of conjugacy class is not a smooth function of anything, and §9.2 shows it must therefore be static. The design contains exactly two discrete fields, $m_K$ and the stratum label, and only the first is permitted to move.

---

## 2. The model layer

Revision 1 hardcoded a single node of the classification and never said so. This section makes the model an input.

### 2.1 Declared inputs

Before any numerical decision, the following are declared and immutable for a run. They are
configuration keys in the module-system sense (§2.5), so R31–R37 govern how they are read,
defaulted, and recorded:

| Input | Example | Consumed by |
|---|---|---|
| $\mathfrak g$ | $\mathfrak{iso}(3,1)$ | everything |
| Group, not just algebra: $\pi_0$, $\pi_1$ | $ISO^+(3,1)$, double cover $\mathrm{Spin}$ | §7.3, §9.4 |
| Maximal reduction $H_{\max}$ | $SO^+(3,1)$ | §9.1 |
| Stratum map $D\mapsto H_D$ | §9.1 menu | §9 |
| Topology of $M$: $H^1(M;\mathbb Z_2)$, $w_2$ | orientable, spin | §5.3, §9.4 |
| Asymptotics | asymptotically flat / de Sitter | §5.4, §11, §12 |
| Physical assumptions | cosmic censorship, NEC | §10.3 |
| Spacetime dimension $d+1$ | $3+1$ | §2.6, §3.1, §4.6 |
| Matter content: fields with their $G$-representations | Dirac spinor in $\mathbf4$; scalar in $\mathbf1$ | §2.7, §7.1 |
| Arithmetic precision and representation | 128-bit, fixed point | §17.6 |

**These are preconditions, not defaults.** §10.3's excision argument is invalid without censorship and the NEC; §5.4's hyperboloidal chart assumes asymptotic flatness; §12 fails outright in de Sitter, where $\mathcal I^+$ is spacelike.

### 2.2 The classifier tuple

From $(\mathfrak g,\mathfrak h)$ and the topology, derive

$$\big(\text{causal type},\ \text{reductive/parabolic},\ \dim\ker\partial^*,\ \dim\mathfrak h,\ d_{\rm contr},\ H^1(M;\mathbb Z_2),\ w_2\big)$$

and dispatch on it. The tuple, not the pair, is what the numerics sees; the classification is a compile-time module and the integrator never branches on `is_parabolic`.

| Datum | Numerical decision |
|---|---|
| Causal type of $\mathfrak z$ (Killing form restricted) | PDE class — see the gate, §2.3 |
| $\dim\mathfrak h$ | count of first-class constraints ⟹ DAE index. Gauge present ⟹ index 2 typically ⟹ no plain explicit RK |
| Reductive vs parabolic | parabolic normality $\partial^* F = 0$ is *algebraic*: eliminate those components exactly each step (index reduction), do not evolve them with a residual |
| Soldered vs gauged generators | discretisation primitive, §7.1 |
| $d_{\rm contr}$ — distance to the nearest orbit-closure boundary | stiffness ⟹ explicit vs IMEX, §7.5 |
| $H^1(M;\mathbb Z_2)$, $w_2$ | which gauge fixings are globally legal, §9.4 |

*Numerics note.* A **DAE** is a differential-algebraic system — ODEs plus algebraic constraints; its **index** is the number of differentiations needed to reach a pure ODE. **IMEX** means implicit on a designated stiff sub-operator, explicit on the rest. **Stiffness** is a wide separation between the fastest timescale in the system and the timescale of interest, forcing a step far below what accuracy alone would require.

**Stiffness from degeneration.** A model near a contraction has widely separated characteristic speeds, which *is* stiffness. Near $c\to\infty$ the ratio is $c/v$ (the low-Mach problem; fix with IMEX or a low-Mach preconditioner). Near $\Lambda\to0$ it is $\ell/L$ with $\ell = \sqrt{3/\Lambda}\approx 1.6\times10^{26}\,\mathrm m$. This is why the orbit-closure *order* is retained as a separate partial order and never symmetrised into an equivalence: a symmetrised version discards exactly the quantity that sets the step.

### 2.3 Applicability gate

Sections 4–13 assume Lorentzian causal type. The gate is not cosmetic:

| Causal type | What survives |
|---|---|
| **Lorentzian** | everything below |
| Riemannian ($\mathfrak{so}(4)$) | §5 slicing, §11 light-cone output, §12.1 confluence-is-causality, §12.3 lookahead all fail. Elliptic BVP; no partial order, hence no schedule freedom |
| Carrollian ($\mathfrak{iso}(3)$) | §5.1 CFL vacuous, §7.5 grading harmless, §8 subcycling pointless. Cones close to worldlines; diamonds degenerate to segments; maximal parallelism |
| Galilean / Bargmann-gauged | cones open to slices; diamonds become slabs; elliptic Poisson solve each step restores the global barrier §12.2 exists to avoid |

The Newton–Cartan contrast is the sharpest statement of why this specification is shaped as it is. Absolute simultaneity would remove the buffer of §11.3, but Newtonian gravity is an elliptic constraint — a global solve, hence a barrier, every step. And with $c\to\infty$ there are no light cones: no causal shielding, so no excision is justifiable on causal grounds; no bounded accuracy horizon; no light-cone output; no $D_A$-driven observer LOD. **The finite light speed that forces the buffer is the same thing that earns causal excision, bounded error propagation, and the parallel architecture.** Every architectural advantage in this document is a rent paid by Lorentzian signature.

A related choice worth naming: soldering the Bargmann central charge gives the 5d Eisenhart lift, a genuinely hyperbolic system with a covariantly constant null Killing vector and no elliptic solve; gauging it gives 4d Newton–Cartan with the barrier. One extra dimension of memory buys removal of the global solve. The classifier can propose this trade only because it retains which generators were soldered.

### 2.4 Two independent axes

Do not conflate:

- **Internal reduction** — shrinking $H$. Changes which components are called gauge. Recovers at most $\dim\mathfrak h \le 6$ functions per site. *Not* a storage win (§9.1).
- **External quotient** — dividing the base by a Killing vector. Removes an entire dimension of storage where an exact symmetry holds.

They compose but are different operations with different justifications. Axisymmetry is the second, never the first.

### 2.5 Realisation in the module system

This document describes a physical scheme; it is realised as modules loaded by a
`CurvedSpacetimeLoader`. The normative contract for that — naming, entrypoints, the dependency
handshake, the configuration format — is
[the Module System Specification](module-system.html), and its requirement identifiers are cited
below as **R*n***. Where the two documents disagree, the module specification governs the
mechanism and this one governs the physics.

| This document | Module system realisation | Governed by |
|---|---|---|
| Declared inputs, §2.1 | keys in `config/<module key>.config`, read once by `ModuleConfig.load()` | R31–R37 |
| Classifier and gate, §15 step 0 | a module's `main` entrypoint; failing the gate throws from `onInitialize()` | R12, R22, R23 |
| Precision and representation, §17.6 | further declared inputs, same mechanism | R31–R37 |
| Evolution stepping, §8 | **not** a `SceneCallback` — see below | — |
| Light-cone output, §11.1 | a `SceneCallback` per scene, supplied by a registered generator | R23 |
| Observer input, §17.2 | the render module's keyboard and mouse interfaces | R17–R19 |
| Asynchronous scheduling, §12 | the entrypoint executor, which must be effectively unbounded | **R28** |
| A module depending on the simulator | a dependent entrypoint registered under `<key_>_dependent` | R17–R19, R25–R27 |

Four consequences are not obvious and are easy to get wrong.

**Declared inputs are configuration, and §2.1's immutability is `load()` being called once.** R33
and R34 require that a missing or unparseable key logs, substitutes a documented default, and
marks the configuration dirty; R35 requires `isDirty()` to report divergence from disk. So §2.1's
"declared and immutable for a run" is not an extra mechanism — it is the existing config contract,
with the addition that nothing may rewrite these values mid-run. A run's identity (§13.3) is
therefore recoverable from its configuration files plus the classifier tuple.

**The renderer's clock and the integrator's clock are different clocks, so the engine carries two
schedulers.** A fixed-rate scheduler drives the existing `MainCallback` and `SceneCallback` at a
wall-clock frame rate — the right home for §11.1's output, for input polling, and for presenting
the most recent completed state. A **dynamic** scheduler, with its own second callback type for
each, drives evolution: a fixed wall-clock rate is a global barrier once per frame, precisely what
§12.2 exists to avoid, and it assumes one global step where §8 requires computational time
$\Delta\tau$ with per-cell $m_K$.

Three requirements on the dynamic type, each of which is load-bearing rather than stylistic.

**Its tick interval is the lapse.** §1's table lists the lapse as "made dynamical by varying node
times $t_k$", and §5.1 gives $h(\xi) = N\,\Delta\tau$. The interval a dynamic callback reports *is*
$N$, not a scheduling convenience that happens to resemble one.

**It must therefore be a function of state, and the API invites the opposite.** A method asking
"when do you next want to run?" is the natural place to write `if (error > tol) halve()`, and §1
forbids exactly that: a step size depending on an error estimator destroys the shadow Hamiltonian
and reintroduces secular drift. The contract belongs in the type's own documentation, not only
here, since the specification is not what is open while the abstract method is being implemented.

**It must be queryable without stepping.** §12.3's conservative scheduling advances a cell only
when no earlier message can arrive, which requires a lower bound on when a neighbour could next
affect it — *before* running it. A returned delay supplies that only in retrospect. Finite $c$
gives the lookahead; a separate query is what lets the scheduler use it.

Constraining reported intervals to powers of two of a base $\Delta\tau$ makes the scheduler a
nested loop with substeps landing on global boundaries and no interpolation at interfaces;
allowing arbitrary intervals requires a priority queue and interpolation at every mismatched face.
§8.1 recommends the former and reserves queues for excision boundaries.

**Light-cone output is the boundary in three senses at once.** It is where §11.1 writes cells as
they cross $\partial J^-(p_k)$; it is where the render module takes over; and by §17.6 it is where
working precision is converted down to whatever the graphics path accepts. Placing the conversion
here rather than earlier means the integrator's precision is invisible to the renderer and can be
changed by configuration without touching it.

**Observer input arrives already in the right frame.** §17.2 requires input expressed in the
observer's own frame, and the render module's input interfaces supply exactly that: cursor
coordinates are positions on the observer's celestial sphere, which by §4.3 is $\partial H^3\cong
S^2$ carrying the conformal structure that transforms by aberration. They must be consumed as
directions on that sphere and **never converted to world coordinates first**, since the conversion
is what would reintroduce the global frame §17.2 prohibits.

**Open.** Whether `Scene` *is* the decorated diamond complex of §3.3 or merely owns one is
undecided, and it determines whether multiple scenes are independent spacetimes or regions of one.
The engine registers a scene callback per scene per generator, so the answer also fixes how many
integrators exist.

**The applicability gate is the engine boundary.** §2.3 already says sections 4–13 assume
Lorentzian causal type, and enumerates what fails otherwise. That is exactly the line between a
general engine and this simulator: the engine supplies §2's model layer, the classifier, the gate
itself, and the machinery that does not care about causal type — §3.1's storage, §7.1's
discretisation primitives, §7.3's Lie-group stepping, §9's stratification machinery, §12's
scheduling, §13's $\mathfrak g^*$-valued residuals. The simulator is what runs once the gate
passes. Nothing in the engine decides the spacetime; a flat Lorentzian, flat Galilean, or curved
version of either is an equally valid declaration, and curved Galilean is Newton–Cartan.

The consequence to plan for is that the engine then needs **two schedulers, not one plus stubs.**
A Galilean node's instantaneous Poisson solve is a global barrier every step (§2.3, §12.2), so the
barrier-free diamond path does not apply there. That path is well-trodden — tree codes, FMM,
multigrid — but it is a second real implementation, not a fallback.

### 2.6 Dimension

$d$ is declared (§2.1). Almost nothing in the construction depends on it.

**Unchanged.** The Klein pair generalises directly — $G \in \{ISO(d,1),\, SO(1,d+1),\, SO(2,d)\}$
for $\Lambda = 0$, $>0$, $<0$. §2.2's classifier tuple dispatches on causal type, $\dim\mathfrak h$
and reductive-versus-parabolic, never on $d$. §4.1's global-section theorem survives because $H^d$
is contractible in every dimension. §4.3's celestial sphere becomes $S^{d-1}$, still carrying a
conformal structure the Lorentz group acts on.

**Changed.** §9.1's little groups become $SO(d)$ timelike, $ISO(d-1)$ null, $SO(d-1,1)$ spacelike.
Storage is $\dim\mathfrak g\times(d+1)$ per site:

| $d+1$ | $\dim\mathfrak g$ | numbers per site | observer cost per shell |
|---|---:|---:|---|
| $2+1$ | 6 | 18 | $\theta^{-1}$ |
| $3+1$ | 10 | 40 | $\theta^{-2}$ |
| $4+1$ | 15 | 75 | $\theta^{-3}$ |
| $9+1$ | 55 | 550 | $\theta^{-8}$ |

with cells scaling as $N^d$ on top. **Cost, not formalism, is the barrier upward.**

**$2+1$ is the validation target.** An order of magnitude cheaper on every axis, with exact
solutions to check against — BTZ at $\Lambda<0$, conical defects from point masses. Vacuum is
*locally flat* there, since Weyl vanishes identically and Riemann is fixed by Ricci, so any local
vacuum curvature is measurably your error. Structurally the Cartan formulation *is* Chern–Simons
theory in $2+1$, so §7.1's holonomies and edge vectors become lattice Chern–Simons rather than an
analogy to it. §15's steps 1–8 are dimension-agnostic and should be exercised here first.

What $2+1$ cannot show is the point of the product: no Weyl means §4.4's tidal floor is empty and
**there are no gravitational waves**, so §17.4's rate dependence does not exist. Validate at $2+1$,
ship at $3+1$.

**Going up is sometimes the optimisation, not the cost.** §2.4's external quotient — dividing the
base by a Killing vector — is Kaluza–Klein reduction, so compact extra dimensions cost nothing and
are never gridded. And §2.3's Eisenhart lift buys removal of a global elliptic solve for one extra
dimension of memory. The classifier can propose that trade only because it retains which
generators were soldered.

**Edges.** At $2+1$ gravity has no local degrees of freedom. At $1+1$ the Einstein tensor vanishes
identically and gravity is empty without a dilaton, though §7.3's matter example is already stated
there. From $4+1$ upward horizon topology is no longer $S^2$ — black rings exist — which §10.3's
excision reasoning assumes without saying so.

### 2.7 Matter content and representations

The Klein pair fixes the geometry; it does not say what lives on it. Matter is a **declared
input** (§2.1): a list of fields with the $G$-representation each carries.

**One primitive covers all of them.** §7.1 currently distinguishes two cases — $\mathfrak h$-valued
components become edge holonomies, soldered $\mathfrak z$-valued components become edge vectors.
Those are the adjoint and vector cases of a single rule: **a field in representation $R$ is
transported by $\rho_R(U_\ell)$.** Stated that way, declaring new matter requires a representation
and no new discretisation.

**Spinors reach back into the gate.** Declaring a spinor field is not free: §9.4 requires
$\mathrm{Spin}$ to be reduced once globally and never per region, and Weitzenböck gauge needs
$w_2 = 0$. So a spinor declaration carries a topological precondition, and the classifier must
check it at the gate (§2.3) rather than discovering it at first use.

**§9.1's node menu is Wigner's classification**, read once. The little groups that select charts —
$SO(3)$ timelike, $E(2)$ null, $SO(2,1)$ spacelike — are exactly the stabilisers classifying
unitary irreps of $ISO(3,1)$ by mass and spin. §9 uses only the chart reading; the same table says
which particle states are available in a region. Worth knowing that stratification and matter
content are governed by one classification rather than two.

Storage per site is therefore §3.1's $\dim\mathfrak g\times(d+1)$ **plus** the declared
representations, and §13.3's run identity must record them.

---

## 3. Geometric setting

### 3.1 Unreduced storage — the central architectural decision

**Store the Cartan connection $A\in\Omega^1(P,\mathfrak g)$ unreduced, $\mathfrak g$-valued, everywhere. Treat $H$ as a chart label, never as a storage format.**

Three consequences, each of which is elsewhere a hard requirement:

1. **Changing $H$ across a face creates and destroys nothing.** It relabels which components are called gauge. This is a canonical change of scheme, not a projection: no degrees of freedom lost, no Mori–Zwanzig memory kernel, no discontinuity in the shadow Hamiltonian. It is exactly the argument §8.4 makes for tolerating $m_K\in\mathbb Z$. Store *reduced* and every $H$-change becomes a projection with memory, and §1 is violated.
2. **§7.1's flux uniqueness forces it.** A face between an $SO(3)$-labelled diamond and an $SO(2,1)$-labelled one must carry a single shared number. The two sides' $\mathfrak h^*$ are different vector spaces, so there is no shared number to have unless fluxes are $\mathfrak g^*$-valued.
3. **There is no obstruction to the stratum assignment itself.** Any labelling is representable. Every constraint in §9 is numerical or diagnostic, never topological.

The cost is $10\times 4 = 40$ numbers per site for $\mathfrak g = \mathfrak{iso}(3,1)$ regardless of $H$, against a best case of $34$ fully reduced. Fifteen percent, repaid immediately in bookkeeping.

### 3.2 The computational manifold

Fix a compact computational manifold $\mathcal C$ with coordinates $\xi^A$ and a **fixed** cell complex. Carry a time-dependent embedding

$$\chi_\tau : \mathcal C \to \Sigma_\tau, \qquad J = \det D\chi, \qquad \rho = J^{-1},$$

with $\tau$ computational time. All resolution adaptivity is motion of $\chi$ ($r$-adaptivity), not insertion or deletion of degrees of freedom ($h$-adaptivity), except where §10 explicitly permits monotone refinement.

1. $\dim T^*Q$ never changes: no projection events, no memory impulses, no discontinuous shadow Hamiltonian.
2. $\mathrm{Diff}_0(\Sigma)$ is connected, so every resolution change is a flow $\chi_\tau = \exp(\tau v)\circ\chi_0$. Smoothness follows from the group structure rather than being imposed.

The mesh velocity $v = \partial_\tau\chi$ **is** the shift $N^i$. Spatial gauge and mesh motion are the same object.

### 3.3 The decorated diamond complex

The computational atom is a causal diamond $D = J^+(p)\cap J^-(q)$. Each carries a stratum label $H_D$, so the complex is a functor from the diamond poset to the classification poset: objects to nodes, faces to arrows.

Two kinds of arrow, and they do not compose alike:

- **Within a conjugacy class** — the arrow is an element of $H_{\max}$, the section space is a smooth coset, and the label may vary continuously. This is the section field of §5.3, already present in Revision 1 as lapse and shift.
- **Between conjugacy classes** — there is no group element relating them; the arrow is a *degeneration* in the specialisation order. §9.2 shows these must be static.

For non-Lorentzian $\mathfrak g$ the tile itself changes shape, which is a clean readout of the gate: the diamond is $J^+\cap J^-$ in whatever causal structure the node supplies.

**Causal selection rule.** §12.1 makes schedule-independence a theorem, and it fails immediately if $H_D$ depends on data that is not schedule-invariant. Therefore

$$\boxed{\,H_D = \mathcal H\big(\text{data in } J^-(D)\big)\,}$$

and nothing else — not a neighbour's latest step, not a global reduction, not an error estimator (§1 forbids that anyway). The confluence test of §13.3 is the gate on this feature and must pass before stratification is enabled.

### 3.4 The mesh as a coframe; bimetric hazard

The monitor of §4 defines a coframe $e^a{}_\mu$ with $M_{\mu\nu} = \delta_{ab}\,e^a{}_\mu e^b{}_\nu$; cells are unit cubes in $e$. The configuration carries **two soldering forms for the same $H$**, which is bimetric gravity, and the mesh potential is a scalar function of the eigenvalues of $g^{-1}M$.

**Ghost condition.** A generic scalar function of those eigenvalues propagates a Boulware–Deser mode. Restrict to dRGT-tuned combinations of the elementary symmetric polynomials, or verify the characteristic analysis directly.

**The criterion that decides it: a reduction is gauge iff it is non-dynamical.** §6.3's fiducial split gives $\delta\mathcal P/\delta g^{\mu\nu} = 0$ identically and so removes the *source* — the gravitational sector is untouched. It does not make the mesh non-dynamical: §6.1 deliberately gives the mesh inertia, so the mesh sector still propagates. Decoupling from gravity and ghost-freedom of the mesh sector are different questions and §16.2 defers only the second.

### 3.5 Holonomy and shear

$\chi$ is a section of a $\mathrm{Diff}_0$-bundle, and the shear accumulated by a circulating compression is nontrivial holonomy of the connection induced by the equidistribution potential.

- **Curl-free construction.** Take $\chi = \nabla\psi$ with $\det D^2\psi = 1/\rho$ (Monge–Ampère). By Brenier this is the unique gradient map transporting the reference density to $\rho$; it lies in the totally geodesic submanifold of gradient maps in Otto's Wasserstein geometry, where holonomy vanishes identically. Swirl is impossible by construction rather than damped.
- **Distortion penalty.** Where circulation is genuinely required (tracking observers on curved worldlines), use the conformal-distortion functional of §6.2.

Existence and reachability of any target $\rho>0$ is Dacorogna–Moser.

---

## 4. The monitor tensor

Resolution demand is a symmetric positive-definite $M$ of dimension $\mathrm m^{-2}$; a cell is correctly sized when it is the unit ball of $M$.

### 4.1 Riemannianisation is the $SO(3)$ reduction

$M$ is not the spacetime metric: $g_{\mu\nu}$ carries no length scale (flat space would give a uniform mesh) and has the wrong signature. Riemannianisation requires a unit timelike congruence,

$$\hat g_{\mu\nu} = g_{\mu\nu} + \frac{2}{c^2}u_\mu u_\nu \quad (u\cdot u = -c^2), \qquad\text{equivalently}\qquad \hat g = g + 2\,\hat u\otimes\hat u \quad (\hat u\cdot\hat u = -1).$$

Check: $\hat g(u,u) = -c^2 + 2c^2 = +c^2 > 0$. **The factor $2/c^2$ silently encodes the normalisation**; with a dimensionless $\hat u$ it must be $2$. Revision 1 stated neither.

Choosing that congruence *is* the reduction $SO^+(3,1)\to SO(3)$, and the section space is $H^3$. Three consequences:

1. **Global well-definedness is a theorem, not an assumption.** $H^3$ is contractible, so all obstructions to a global section vanish and the monitor is globally definable on any $M$ admitting a Lorentzian structure. Revision 1 asserted this. (For $O(3,1)$ rather than $SO^+(3,1)$ the coset is the two-sheeted hyperboloid, $\pi_0 = \mathbb Z_2$, and the section exists iff $M$ is time-orientable — obstruction in $H^1(M;\mathbb Z_2)$. Declare the group, §2.1.)
2. **The monitor is a field on observer space $\mathcal O^7 = P/SO(3)$, not on $M^4$.** $\sum_k$ in §4.2 is a sum over points in the $H^3$ fibre. That the computation happens on a 4-dimensional section is an economy, not a fact about the object.
3. **Which congruence?** $n^\mu$, the slicing normal (section-adapted; the floor term is then unambiguous and observer terms are pullbacks), or $u_k$ per observer (so the sum combines forms built from different Riemannianisations)? Both defensible, **not the same monitor**, differing at $O(\gamma^2)$ for relativistic observers. This specification takes $n^\mu$ for the floor and $u_k$ for the observer terms, and requires the choice to be stated in any results.

**Fibre-orthogonality limit.** The $\sqrt n$ bound in §4.2 assumes demands comparable in the fibre. Two observers at large relative rapidity over the same event have nearly $H^3$-orthogonal demands; the smooth max degrades and no 4-dimensional tensor satisfies both. Either bound the relative rapidity across the observer set or report the compromise quantitatively. Genuinely satisfying both would require resolving the boost directions, i.e. computing on $\mathcal O^7$.

### 4.2 Composition rule

$$\boxed{\;M = \lambda(\tau)\sum_k \frac{\Pi_k^\perp}{\theta_k^2\,D_{A,k}^2} \;+\; \underbrace{\frac{16}{\lambda_J^2}\,\hat g + \alpha\,|E_{ij}|}_{\text{validity floor}}\;}$$

Demands add as $\Delta x^{-2}$: a smooth tensorial maximum, within $\sqrt n$ of true metric intersection and $C^\infty$ everywhere, unlike intersection which is non-differentiable at eigenvalue crossings. No jumps; overlapping observers do not harmfully double-count.

Units check: $\theta$ dimensionless, $D_A$ in metres, $\hat g$ dimensionless with $x^0 = ct$, $E_{ij} = C_{i0j0}$ at $\mathrm m^{-2}$, $\alpha$ dimensionless. All terms $\mathrm m^{-2}$.

### 4.3 Observer term

Acuity $\theta_k$ at angular-diameter distance $D_{A,k}$ gives transverse demand $\Delta x_\perp = \theta D_A$, with $D_A$ obeying the Sachs focusing equation along the observer's past light cone,

$$\frac{d^2 D_A}{d\lambda^2} = -\left(\tfrac12 R_{\mu\nu}k^\mu k^\nu + |\sigma|^2\right) D_A .$$

**Lensing is therefore automatic**: magnified regions subtend more sky and are refined without special handling. Cap the monitor near caustics, where $D_A\to0$ and the demand diverges.

**Acuity is a conformal structure on the celestial sphere.** $\theta$ lives on $\partial H^3 \cong S^2$; $SO^+(3,1)$ acts there by Möbius transformations; the aberration factor is the conformal factor

$$\mathcal D = \big[\gamma(1-\beta\cos\vartheta)\big]^{-1} \;\longrightarrow\; 2\gamma \ \text{ forward}.$$

Aberration bunches the forward sky, relaxing transverse spatial demand ahead by $\sim2\gamma$, while Doppler blueshift tightens temporal demand ahead by the same factor. Demand rotates between space and time under boost — which is why the monitor must be a tensor and not a scalar density. $\Pi_k^\perp$ is the aberration-corrected transverse projector, i.e. the pullback of the round metric on $S^2$.

Structurally the observer term sits at the *parabolic* node $(\mathfrak{so}(3,1),\mathfrak{sim}(2))$ with $G/P = S^2$, and $\theta$ is a choice of scale — a Weyl structure. §4.5 depends on this.

### 4.4 Validity floor

- **Jeans term.** $\lambda_J = c_s\sqrt{\pi/G\rho_m}$ resolved by $\gtrsim4$ cells (Truelove), hence $16/\lambda_J^2$. This, not curvature, binds on self-gravitating matter: curvature gives $L_R\sim c/\sqrt{G\rho_m}$, so $L_R/\lambda_J\sim c/c_s\approx1.5\times10^6$ for $c_s\sim0.2\,\mathrm{km\,s^{-1}}$ molecular gas. A curvature-calibrated mesh under-resolves cold gas by six orders of magnitude. Curvature supplies the correct *time* scale ($L_R/c\sim t_{\rm ff}$) and the wrong *space* scale.
- **Tidal term.** $E_{ij} = C_{i0j0}$, the electric Weyl tensor, from Gauss–Codazzi in ADM variables,
  $$E_{ij} = R^{(3)}_{ij} + K K_{ij} - K_{ik}K^k{}_j - (\text{matter terms}),$$
  so only spatial derivatives enter and the mesh equations remain second order — no Ostrogradsky mode. Its eigenframe supplies anisotropy axes; cells elongate along tidal directions automatically.
- **Ricci** is algebraically $T_{\mu\nu}$ and is the relativistic completion of the Jeans floor, not a substitute for it.

### 4.5 Budget control

Total demand $\mathcal N_{\rm req} = \int_\Sigma\sqrt{\det M}\,dV$. When it exceeds the node count, reduce the single scalar $\lambda(\tau)\le1$, which scales **only** the observer term. Observer detail is an objective — degrading it blurs the picture but leaves the simulation valid. The floor is a constraint — violating it makes the discrete dynamics converge to the wrong PDE, and that error propagates outward at $c$ into every observer's past light cone.

**Why smooth degradation is always available.** By §4.3, $\lambda$ is a Weyl rescaling of the celestial conformal structure. The space of Weyl structures is an affine space modelled on $\Omega^1$ — contractible — so a global smooth rescaling always exists and any two differ by a 1-form. Revision 1 asserted smooth degradability; this is why it holds.

### 4.6 Cost laws

**Corrected.** The familiar $\mathcal N_{\rm obs}\simeq(4\pi/\theta^3)\ln(R/r_0)$ presumes *isotropic* demand $\Delta x_\perp = \Delta r = \theta r$. But the observer term carries $\Pi_k^\perp$ — transverse only. Radial spacing is not observer-driven at all. If it were floor-limited the cost would be $\propto R$, linear, and the scale-free claim would fail outright.

The actual resolution is §5.4: the radial direction is handled by *compactification*, not by adaptivity. So

$$\mathcal N_{\rm obs}\simeq \frac{4\pi}{\theta^2}\times N_r(\text{height function}) \quad\text{per observer,}$$

with $\theta^{-2}$ per shell and the radial count set by the hyperboloidal chart, which is logarithmic-like but whose coefficient is *not* $\theta^{-1}$. The scale-free conclusion survives; the $\theta^{-3}$ bookkeeping does not. In $\xi = \ln r$ observer demand is uniform — an observer hotspot is not a hotspot in the right chart.

Self-gravity has no such property. $\lambda_J\propto\rho_m^{-1/2}$ shrinks without bound and $t_{\rm ff}$ with it; resolving a collapsing region better reveals higher density, tightening the requirement further. **Fixed node count cannot follow gravitational collapse indefinitely.** This is a statement about finite degrees of freedom, not a numerical weakness. See §10.

---

## 5. Slicing

### 5.1 Lapse from the Jacobian

**Corrected.** Revision 1 wrote $N = \sigma J^{\beta/d}\Delta x^{1-\beta}/c$ with $\Delta x = J^{1/d}$, which collapses to $\sigma J^{1/d}/c$ identically: $\beta$ was inert, while §5.3 and §6.4 both treated it as live. Introduce a fixed reference length $\ell_0$:

$$\Delta x(\xi) = J^{1/d}, \qquad \boxed{\;N(\xi) = \frac{\sigma}{c}\,J^{\beta/d}\,\ell_0^{\,1-\beta}\;}, \qquad h(\xi) = N\,\Delta\tau .$$

- $\beta = 1$: $N = \sigma\Delta x/c$. Stability limit satisfied everywhere at uniform Courant number $\sigma$, for free — small cells receive small physical time steps. Uniform $\Delta\tau$, non-uniform $\Delta t$.
- $\beta = 0$: $N = \sigma\ell_0/c$, uniform physical step, zero tilt accumulation, cost $\propto r$ in the compression.
- Intermediate $\beta$ trades global cost $r^{1-\beta}$ against tilt rate $(1-r^{-\beta})$.

Smooth $N$ with $(1-\ell^2\Delta)^{-1}$ before use, to limit tilt accumulation from short-wavelength structure in $J$.

### 5.2 Why arbitrary slices are legitimate

For Cauchy surfaces $\Sigma_0,\Sigma_1$ bounding a slab $\Omega$, define

$$S(\phi_0,\phi_1) = \operatorname*{ext}_{\phi|_\partial}\int_\Omega\mathcal L, \qquad \pi_1 = \frac{\delta S}{\delta\phi_1},\quad \pi_0 = -\frac{\delta S}{\delta\phi_0}.$$

$S$ is a type-1 generating function, so $\Phi: T^*Q_{\Sigma_0}\to T^*Q_{\Sigma_1}$ is symplectic. **The only hypothesis is that both surfaces are Cauchy**; their shape is free. This is Tomonaga–Schwinger, with slice-independence guaranteed in the continuum by the hypersurface deformation algebra.

Logical order: the primitive is multisymplecticity, $\partial_\tau\omega + \partial_i\kappa^i = 0$, a statement about the Cartan form on $J^1Y$ and the cell complex, independent of any foliation. Symplecticity on a slice is its *pullback*. Design to the covariant statement and slicing freedom is automatic.

### 5.3 The tilt bound is geometry on $H^3$

Write the slice as $t = T(\tau,\xi)$ with $\partial_\tau T = N$. Spacelikeness requires $c|\nabla_\Sigma T| < 1$, and since $\partial_\tau(\nabla T) = \nabla N$, tilt **accumulates from lapse gradients**. For compression ratio $r$ and transition width $w$,

$$\boxed{\;t_{\rm far} < \frac{w}{c\,(1-1/r)}\;}$$

This is collapse of the lapse / slice stretching, identical to the puncture pathology in numerical relativity because it is the same equation. It is a *geometric* obstruction: beyond the bound no global spacelike foliation with those lapses exists, and no choice of $N$ repairs it.

**Governing dichotomy.**

- **Comoving compressions** (shocks, solitons, tracked observers): $\Delta T\approx(\ell/v)(1-1/r)N_{\rm far}$ — bounded, independent of run time. Uniform $\Delta\tau$ indefinitely.
- **Stationary compressions** (bound clumps, collapsing cores, excision neighbourhoods): secular. Requires intervention (§8.3, §10).

**The structural change.** $c|\nabla_\Sigma T| < 1$ is exactly the condition that the section stays in the fibre; $|\nabla T|\to 1/c$ is $\partial H^3$, the celestial sphere at infinity. Revision 1 enforced this with a coordinate log barrier $-\lambda_t\int\log(1-c^2|\nabla T|^2)$. **Replace it with the geometry.** Parametrise the slicing by $\zeta\in H^3$ — the boost from a reference frame — and step with a Lie-group (Runge–Kutta–Munthe-Kaas) integrator on $SO^+(3,1)/SO(3)$:

$$\zeta_{n+1} = \exp_{\zeta_n}\!\big(\Delta\tau\,\Xi_n\big), \qquad \Xi_n\in T_{\zeta_n}H^3 .$$

*Numerics note.* **RKMK** takes the Runge–Kutta increment in the Lie algebra and pushes it forward by $\exp$, so the numerical solution never leaves the group or coset. Needs $\exp$ and $\mathrm{dexp}^{-1}$ per algebra; $\mathfrak{so}(3,1)$, $\mathfrak{so}(1,4)$, $\mathfrak{iso}(3,1)$ all have closed Rodrigues-type forms.

Consequences:

- $H^3$ is geodesically complete, so **spacelikeness is unconditional**, not barrier-enforced. The failure mode of an explicit step jumping a log singularity disappears — there is no singularity to jump.
- $\lambda_t$ leaves §6.4. One fewer fiddly weight.
- The bound above becomes accumulated geodesic distance in $H^3$ at rate $|\nabla N|$: same content, but with a metric on the space of slicings, so "how far from failure" is a number rather than a proximity to a divergence.
- The $\beta\to0$ self-regulation still happens, as geodesic slowdown near the boundary — the honest version of it.

The volume barrier $-\lambda_v\int\log J$ is a different matter; $\{J>0\}$ has a genuinely reachable boundary and needs §7.2.

### 5.4 Observer slicing

For each observer worldline adopt **hyperboloidal slicing with radial compactification**: a height function with $c|H'|\to1$ and $r = \varrho/(1-\varrho^2/S^2)$. Then:

- tilt is *stationary by construction* rather than accumulating — chosen once, never integrated;
- outgoing coordinate light speed stays $O(1)$ while ingoing $\to0$ at $\mathcal I^+$, so uniform $\Delta\tau$ satisfies the stability limit everywhere;
- there is no outer boundary, hence no reflection — important because a dissipation-free scheme conserves reflected error forever;
- outgoing radiation redshifts onto the coarsening grid rather than aliasing against it.

**This is also how the null stratum is reached.** By §7.5 you cannot *grade* into $\mathfrak{sim}(2)$ — it requires $\eta\to\infty$. Compactification maps infinite rapidity to finite coordinate, which is why hyperboloidal slicing reaches $\mathcal I^+$ at all. The fibre compactification and the base compactification are the same move. Treat the null node as an asymptotic boundary condition, never as a stratum to refine into.

The dynamical mesh map is reserved for **translation** of the chart along the worldline. The radial profile is geometry, not adaptivity.

### 5.5 Observer back-reaction

- **Dynamical observer** ($q_{\rm obs}\in T^*Q$): the system stays autonomous and energy is conserved, but the observer drags its mesh distortion and acquires a polaron mass $m^\star = m + \mu\int_\mathcal{C}|\partial_{q_{\rm obs}}\chi|^2$. Mitigate with small mesh inertia and fast critical damping, or measure $m^\star$ once and renormalise.
- **Prescribed worldline**: no back-reaction, but $H$ becomes non-autonomous. Still symplectic; energy bounded if $\tau_\rho\ll c/a$, secular otherwise. **These two options are not merely a performance trade.** A prescribed observer appears in $L$ as external structure and breaks the gauged Poincaré symmetry *explicitly*; a dynamical one breaks it only *spontaneously*, through its state. See §17.1 — the difference decides whether the run is GR at all.
- **Constant velocity**: free. Boost to the comoving frame and $E - v\!\cdot\!P$ is exactly conserved on a boosted-flat slicing.

---

## 6. The action

### 6.1 Total

$$L[\phi,\chi] = \int_{\mathcal C}\left[\frac{1}{2N}\left|\partial_\tau\phi - \mathcal L_v\phi\right|_g^2 - \frac{N}{2}\left|\nabla\phi\right|_g^2\right] J\,d^d\xi \;+\; \frac12\int_\mathcal{C}\mu(\xi)\left|\partial_\tau\chi\right|^2 d^d\xi \;-\; \mathcal P[\chi]$$

The field and the mesh form **one** Hamiltonian system on $T^*(\mathcal F\times\mathrm{Diff}_0)$. Discretise this whole object variationally and multisymplecticity follows with no additional argument.

The mesh kinetic term is the key structural choice. An elliptic equidistribution solve each step would be an external controller (§1) and would impose a global synchronisation barrier (§12). Giving the mesh **inertia** makes $\rho$ obey a second-order equation, so $\dot\rho$ is bounded and an instantaneous jump costs infinite action. This is the field-theoretic analogue of Hairer–Söderlind reversible adaptivity.

Set $\mu(\xi)\propto J^{-1}$ so the mesh's own wave speed is uniform in physical space; with uniform $\mu$ the barrier force $\sim\lambda/J$ makes the mesh the stiffest subsystem exactly inside compressions, and *its* stability limit, not the field's, sets the global step.

### 6.2 Mesh potential

$$\mathcal P[\chi] = \frac{1}{2\epsilon}\int_\mathcal{C}\big(J^{-1} - w[\phi]\big)^2 \;-\; \lambda_v\!\int_\mathcal{C}\log J \;+\; \lambda_s\!\int_\mathcal{C}(K-1)^2 ,$$

$$w[\phi] = \sqrt{\det M}\,,\qquad K(\chi) = \frac{\|D\chi\|_F^{\,d}}{d^{d/2}\det D\chi}\;\ge\;1 .$$

**The tilt barrier is gone** — §5.3 handles spacelikeness geometrically, unconditionally, and without a weight.

- **Equidistribution** drives $\rho$ toward the monitor volume.
- **Volume barrier** ($\log J$) penalises $J\to0$. Note the caveat in §7.2: this is a continuum guarantee only.
- **Distortion penalty**: $K\ge1$ with equality iff $D\chi$ is conformal. The volume barrier does not prevent needle cells at healthy $J$; this does. It is soft, not a barrier, so needle cells are penalised but reachable — pair with the §13.3 monitor on $\max K$.

**Monitor smoothing is not optional.** $w$ evaluated on a compressing mesh resolves gradients better, reads them larger, and compresses further — genuine positive feedback. Smooth $w$ spatially and impose a hard cap $r_{\max}$.

### 6.3 Fiducial metric

If gravity is dynamical, build $\mathcal P$ from a fiducial $\bar g$ (bimetric split $g = \bar g + h$, with $\bar g$ evolved by an independent hyperbolic gauge rule). Otherwise $T^{\rm mesh}_{\mu\nu} = -\frac{2}{\sqrt{-g}}\,\delta\mathcal P/\delta g^{\mu\nu}\ne0$ and the simulation solves a modified theory with a spurious source — the polaron becomes a fictitious gravitating fluid. If an $O(\mu)$ modification is accepted instead, bound it explicitly and report it. See §3.4 for what this does and does not settle.

### 6.4 Parameters

| Symbol | Meaning | Guidance |
|---|---|---|
| $\sigma$ | Courant number | $\lesssim0.4$; resonance (§8.4) usually binds first |
| $\mu$ | mesh inertia | small; sets $m^\star/m$ |
| $\epsilon$ | equidistribution stiffness | $\tau_\rho = \sqrt{\mu\epsilon}$ |
| $\tau_\rho$ | mesh response time | below the field's fastest timescale and below $c/a$ for accelerating observers; **not** so far below that the mesh becomes stiff |
| $\lambda_v,\lambda_s$ | barrier / penalty weights | as small as keeps the invariants satisfied |
| $\ell_0$ | lapse reference length | fixed per run; only enters via $\beta\ne1$ |
| $r_{\max}$ | compression cap | required, not advisory |
| $\beta$ | lapse exponent | $1$ where tilt permits, lower near stationary compressions |
| $\lambda$ | observer quality | dynamic, $\le1$ |
| $a,b$ | split parameters | §9.5 — **must be declared**, no default |

$\lambda_t$ is retired (§5.3). Critical damping $\mu/\epsilon$ remains the one genuinely fiddly tuning and should be set per problem class.

---

## 7. Discretisation

### 7.1 Variational, in flux form, $\mathfrak g^*$-valued

Discretise $S_d = \sum_{\rm cells}L_d$ over the spacetime complex. Two facts do the work:

**Telescoping.** Summing a flux-form update over any union of cells, interior faces appear twice with opposite orientation and cancel; only the outer boundary survives. This is discrete Stokes, exact because $\partial\partial = 0$ — a cochain identity on the complex, independent of the metric, the foliation, and the schedule. Regge calculus and discrete exterior calculus take this as primitive.

**Flux uniqueness.** Each face must carry a *single* number shared by both neighbours. This is the only requirement for exact conservation, and it replaces global synchronisation (§12).

**Face fluxes and momentum maps are $\mathfrak g^*$-valued.** Revision 1 stated the momentum-map residual in $\mathfrak h^*$, which across a stratum interface compares different vector spaces. Use the momentum map for the full $\mathfrak g = \mathfrak{iso}(3,1)$ and project to the local $\mathfrak h$ for display only. This makes §13.1's diamond residual stratum-independent, which is what keeps the primary diagnostic valid across the whole scheme.

The multisymplectic law $\partial_\tau\omega + \partial_i\kappa^i = 0$ is already in flux form; slice symplecticity is its telescoped, foliation-dependent shadow. Discrete Noether gives per-cell momentum-map conservation with flux.

**Primitives by representation.** The general rule is that **a field in representation $R$ is transported by $\rho_R(U_\ell)$**; the classifier's soldered/gauged split is the adjoint and vector case of it, and fixes the storage:

- $\mathfrak h$-valued components → **edge holonomies** $U_\ell\in H_{\max}$, Wilson-style. Gauge covariance exact; no drift in the Gauss constraint.
- $\mathfrak z$-valued (soldered) components → **edge vectors** $e^a_\ell\in\mathfrak z$. This is Regge calculus, and it is Regge calculus *because* $e$ is tensorial and invertible.
- declared matter in any other representation (§2.7) → transported by $\rho_R$ of the same edge holonomies. New matter needs a representation, not a new discretisation.

Advance both with RKMK (§5.3) so frames stay orthonormal to machine precision with no re-orthonormalisation, and pair with the discrete-variational scheme so discrete Noether gives exact momentum-map preservation structurally rather than by constraint damping.

*Numerics note.* "Conservative form" means the update reads $u^{n+1}_K = u^n_K - (\Delta\tau/V_K)\sum_{\rm faces}F$: a cell changes only by what crosses its faces. Lax–Wendroff: a conservative scheme that converges, converges to a weak solution with correct shock speeds; non-conservative schemes get shock speeds wrong. Flux form is not bookkeeping.

### 7.2 Domains are not constraints

$\{J > 0\}$ and $\{\det e\ne0\}$ are **open, non-convex** sets, not constraint surfaces. Two consequences that Revision 1 missed.

**A log barrier is a continuum guarantee only.** "$\chi_\tau$ provably remains a diffeomorphism for all $\tau$" holds for the flow, not for a finite step. A symplectic step is a polynomial map that does not know about the singularity at $J=0$; explicit integrators on log barriers step through them routinely. §1 forbids the standard fix, since a rejection-based line search is a discontinuous function of state and kills the shadow Hamiltonian. Three admissible fixes:

1. **Split off the barrier and integrate it exactly.** The $-\lambda_v\log J$ flow is analytically solvable in the volume direction; use it as one leg of a Strang splitting. Domain preservation becomes exact rather than asymptotic. *Preferred.*
2. **Step on $GL^+$ geodesically** — RKMK with the update taken in $\mathfrak{gl}$ and pushed forward by $\exp$, so $\det > 0$ by construction.
3. **Smooth step-size guard.** Legal under §1 precisely because §1's criterion is smoothness on phase space, not absence of state dependence. Make the guard a $C^\infty$ function of $J$, never a threshold test.

**Componentwise interpolation is unsafe.** Two admissible $D\chi$ can average to a singular one — $\mathbb 1$ and $-\mathbb 1$ in $SL(2)$ is the minimal example. So AMR prolongation, multistep restarts, and any blend across the buffered grading of §8.4 can manufacture a tangled cell from two healthy ones. **Interpolate by polar decomposition or in the log (geodesic averaging on $GL^+$), never componentwise.**

### 7.3 Manifold-valued targets

Where the field takes values in a manifold $(\mathcal N,g_{ab})$ — sigma models, unit vectors, orientations — additive updates leave the target and the Hamiltonian is non-separable, so operator splitting is unavailable. Both are solved by a position–position discrete Lagrangian built from geodesic distance, which never asks for a kinetic/potential split. In $1+1$ with $\lambda = (\Delta t/\Delta x)^2$:

$$\phi_i^{n+1} = \exp_{\phi_i^n}\!\left(\lambda\left(\log_{\phi_i^n}\!\phi_{i+1}^n + \log_{\phi_i^n}\!\phi_{i-1}^n\right) - \log_{\phi_i^n}\!\phi_i^{n-1}\right).$$

Explicit; on-manifold exactly; multisymplectic; exact momentum maps for target isometries, since $d(\cdot,\cdot)$ is invariant. Valid within the injectivity radius — past the cut locus $\log$ is undefined and the scheme must fall back to a retraction (any second-order retraction preserves the order). Lie-group targets: link variables $U_i^n = (g_i^n)^{-1}g_{i+1}^n$ with the Cayley transform for quadratic groups, so group membership is exact and rational.

**This is the same code path as the geometric sector.** §7.1's holonomies and §5.3's $H^3$ stepping are instances of this construction, not analogues of it. Implement once.

Topological charge on $S^2$ targets: Berg–Lüscher signed spherical-triangle areas, exactly integer-valued, changing only through degenerate triangles — a genuine discrete-topology diagnostic rather than a drifting float. Note this is a $\pi_2$ statement about the target and is sensitive to the declared group (§2.1), not merely the algebra.

### 7.4 Non-separability from the lapse

$\bar H = N(q,p)(H + p_t)$ is non-separable, so explicit splitting is unavailable. Options: Mikkola's logarithmic-Hamiltonian choice of $N$; Tao's phase-space doubling with a binding constraint; or a symmetric implicit definition of $N$ preserving reversibility. Retain reversibility regardless — see §12.4.

### 7.5 One grading bound for three gradings

Varying $\Delta x$ is a varying discrete dispersion relation, i.e. a refractive-index profile. Steep grading partially reflects short wavelengths; reflection is exponentially small only if grading is adiabatic. Revision 1 bounded per-cell size ratios at $\lesssim1.15$.

**The same bound governs frame grading.** Adjacent diamonds adapted to $u_1,u_2$ at relative rapidity $\eta$: a mode crossing the face is Doppler-rescaled by $e^{\eta}$ longitudinally. Same $\Delta x$, different apparent $\lambda$ — which is *exactly* a cell-size jump of ratio $e^\eta$. Therefore

$$r = e^{\Delta\eta}\lesssim1.15 \;\Longrightarrow\; \boxed{\;\Delta\eta\lesssim0.14\ \text{per face, i.e. }\Delta v\lesssim0.14\,c\;}$$

Transverse modes see only $\cosh\eta = 1+\eta^2/2$, so the longitudinal direction binds. **Cell size, $m_K$, and frame orientation are three gradings with one requirement: adiabaticity of the discrete dispersion relation.**

Worked figure: $\gamma = 10$ is $\eta = \operatorname{arccosh}10\approx2.99$, about $21$ buffer cells. Affordable. $\eta\to\infty$ is not — hence §5.4.

The sting specific to this design: a dissipative scheme damps reflected error; a symplectic one conserves it forever. Structure preservation makes grading quality *more* critical, not less.

Note also that the modified-energy band $O(h^p)$ is set by the coarsest region, so a deep hotspot can widen the global energy oscillation even while improving the region of interest.

---

## 8. Time stepping

### 8.1 One scheme, one integer field

Uniform $\Delta\tau$ is the asynchronous scheme with all cell clocks equal. Write a single discrete action

$$S_d = \sum_K\sum_{j=0}^{m_K-1}L_d\!\left(\phi|_K;\ \tau^n + j\,\Delta\tau/m_K\right),\qquad m_K\in\{1,2,4,8,\ldots\},$$

with $m_K$ an integer field on the complex. $m_K\equiv1$ is bulk-synchronous; large incommensurate $m_K$ is a full asynchronous variational integrator (AVI). There is no seam between two methods to manage.

*Numerics note.* "Subcycling" means a cell takes several small steps for every one step taken elsewhere.

**Keep $m_K$ a power of two.** Patch substeps then land exactly on global step boundaries, no interpolation is needed at interfaces, and the scheduler is a nested loop rather than a priority queue. Reserve genuine per-element queues for regions where the mesh is unstructured enough that commensurability is unnatural — principally excision boundaries.

### 8.2 Interfaces are conservative by construction

In conventional adaptive codes a fast/slow boundary leaks: the coarse side computes one flux across a shared face, the fine side computes $m$, and the mismatch must be corrected by hand (Berger–Colella *refluxing*), a standard source of bugs.

Here, varying the action with respect to a shared interface node yields a *single* stationarity condition already summing every adjacent contribution, coarse and fine. Both sides see the same number by construction. Refluxing is derived, not implemented. And $r$-adaptivity produces no hanging nodes, removing the spatial version of the problem.

### 8.3 Deployment rule

Uniform $\Delta\tau$ already handles varying resolution — the stability limit is satisfied everywhere by §5.1. Local asynchrony is *not* needed for that. It is needed where §5.3 fails:

| Situation | Treatment |
|---|---|
| Comoving compression | uniform $\Delta\tau$, indefinitely |
| Stationary compression, low fidelity needed | sink + reservoir (§10.2) |
| Stationary compression, interior irrelevant | excision (§10.3) |
| Stationary compression, interior required | **local AVI patch** |

An AVI patch evades the tilt bound because the discrete spacetime need only be a partially ordered set whose order contains the numerical light cones — strictly weaker than admitting a global spacelike foliation. Multisymplecticity is per-cell and survives this exactly.

### 8.4 Hazards

- **Multirate resonance.** Instability when $\Delta\tau$ approaches half the period of a fast mode inside a patch — the mode is kicked in phase each cycle. This caps r-RESPA in molecular dynamics and binds *before* the ordinary stability limit. Size $\Delta\tau$ against the patch's fastest period.
- **Ratio-boundary reflection.** A jump in $m_K$ changes the discrete dispersion relation exactly as a jump in cell size does. Grade $m_K$ through a buffer ($8,4,2,1$ over several cells), never $1\to8$. Same bound as §7.5.
- **Chatter.** $m_K\in\mathbb Z$ is the only *moving* discreteness in the design (the stratum label is discrete but static, §9.2). It is mild — no degrees of freedom created or destroyed, so a canonical change of scheme rather than a projection, no memory kernel — but each change shifts the modified Hamiltonian and hence the energy band. A cell oscillating across a threshold random-walks those shifts into secular drift. **Use hysteresis** (different thresholds up and down) and stagger transitions across cells so no coherent kick forms. Diagnose by counting threshold crossings per cell per light-crossing time and alarming on *coherence*, not on the count: correlated kicks are the failure, individual kicks are not.
- **Weaker theory.** Stability for fully asynchronous schemes is less well characterised than for synchronous ones; the guarantees are multisymplecticity and momentum maps, not a clean stability bound. Leave margin and test per problem.

---

## 9. Stratification

New in Revision 2. This section governs the assignment $D\mapsto H_D$.

### 9.1 The node menu

For $\mathfrak g = \mathfrak{iso}(3,1)$, $H_{\max} = SO^+(3,1)$, the usable nodes are the **Wigner little groups** — stabilisers of a timelike, null, or spacelike vector — plus the trivial and maximal cases. Section space is $H_{\max}/H$. In $d+1$ dimensions these become $SO(d)$, $ISO(d-1)$ and $SO(d-1,1)$ (§2.6).

This table is read twice. Here it selects charts; it is simultaneously Wigner's classification of unitary irreps by mass and spin, so it also says which matter states are available in a region (§2.7).

| $H$ | $\dim$ | Distinguished direction | Section space | Use |
|---|---|---|---|---|
| $SO^+(3,1)$ | 6 | none | — | vacuum far field; no section to degenerate, no tilt bound, but no time function either |
| $SO(3)$ | 3 | timelike (massive) | $H^3$ | ADM/BSSN; strong-field interiors; moving punctures. **Default** |
| $\mathrm{Sim}(2)$ | 4 | null ray | $S^2$ | asymptotic boundary condition only, §5.4. Parabolic |
| $E(2) = ISO(2)$ | 3 | null vector (massless) | null cone | characteristic evolution, wave zone |
| $SO(2,1)$ | 3 | spacelike (tachyonic) | $dS_3$ | axis-adapted regions: jets, wires, strings |
| $SO(2)$ | 1 | timelike + spacelike | — | 2-frame adapted; axisymmetry |
| $\{e\}$ | 0 | full parallelism | $SO^+(3,1)$ | teleparallel; **requires $w_2 = 0$** |

Note $\dim M_{\rm base} = \dim\mathfrak g - \dim\mathfrak h$, so shrinking $H$ *raises* the base dimension: $SO(3)$ gives observer space $\mathcal O^7$, not a 4-manifold. §4.1 explains why that is the right home for the monitor and why computing on a 4-dimensional section is an economy.

**The saving is characteristic structure, not storage.** Storage is fixed at 40 numbers per site (§3.1). What varies by orders of magnitude:

- In the wave zone a spacelike-adapted chart must resolve outgoing radiation at wavelength $\lambda$; a null-adapted chart makes outgoing characteristics stationary and the radiation stops oscillating in the chart. §5.4 already makes this argument for the hyperboloidal profile; naming the node generalises it from a large-$r$ trick to a local reduction.
- CFL follows the fastest characteristic *in the chart*: null-adapted gives outgoing $O(1)$, ingoing $\to0$, so $\Delta\tau$ is set by geometry rather than by $\lambda$.
- $\dim\mathfrak h$ is the count of first-class Gauss constraints to monitor, so smaller-$H$ nodes are cheaper in §13.3 but commit to a section with a reachable boundary.

For genuinely large savings use the *external* axis (§2.4).

### 9.2 Static strata, dynamical sections

The stratum label is discrete-valued, hence inherently a discontinuous function of state — §8.4's chatter problem, and worse than $m_K$ because the classification poset is not totally ordered, so there is no one-dimensional hysteresis band.

**The grading bound dissolves this rather than managing it.** Transitions between conjugacy classes are *degenerations*, not group elements: boosting a timelike direction with rapidity $\to\infty$ gives $SO(3)\rightsquigarrow E(2)$, and the same limit from spacelike gives $SO(2,1)\rightsquigarrow E(2)$. The null node sits on the boundary of both orbits in $\mathrm{Sub}_3$ — the massless little group is a contraction of the massive one, and the orbit space is non-Hausdorff there. By §7.5 the crossing costs $\eta\to\infty$. **No dynamical trajectory crosses a stratum boundary.**

Therefore:

- **Stratum label: static, assigned from geometry before the run.** Timelike/$SO(3)$ in strong-field interiors; null/$\mathrm{Sim}(2)$ asymptotically via §5.4; spacelike/$SO(2,1)$ on axes; full $SO^+(3,1)$ in vacuum far field.
- **Section within a stratum: dynamical and smooth**, the $H^3$-valued field of §5.3, which is already the lapse and shift.

No second moving discrete field, no hysteresis on a graph, no shadow-Hamiltonian breakage, and §14.4's "one discontinuity survives" remains true as written. Assignment must still respect §3.3's causal selection rule if it is ever computed rather than declared.

### 9.3 Interfaces

A static stratum interface is a **Cauchy–characteristic matching** interface, and inherits that technique's accumulated experience rather than its bugs. Expect stiffness ratio $\sim\gamma$ across it, since the transition parameter is rapidity (§2.2). Budget IMEX there.

The degeneration is one-way in the orbit-closure order: you cannot smoothly return from the null node along the same path. If transitions are ever made dynamic, the transition graph must be **directed** with asymmetric costs — ordinary two-threshold hysteresis is not the right structure.

### 9.4 Checks the classifier must run

- **Effectiveness.** $\mathfrak h$ must contain no nonzero ideal of $\mathfrak g$, else the geometry is really that of $(\mathfrak g/\mathfrak n,\mathfrak h/\mathfrak n)$. Since Lorentz acts irreducibly on $\mathbb R^{3,1}$, the only ideals are $0$ and the translations, so the test reduces to: **$\mathfrak h\not\supseteq\mathbb R^{3,1}$**. Cheap; silent and catastrophic if skipped.
- **Spin structures do not refine per region.** Reduce $\mathrm{Spin}(3,1)$ once, globally. Never build separate spin structures per node: $\pi_1(SO(3)) = \mathbb Z_2$ but $\pi_1(E(2)) = \mathbb Z$, and naive per-region spin bundles will not glue.
- **Legality of each gauge fixing.** Compile-time from the declared topology:

| Fixing | Legal iff | Source |
|---|---|---|
| time gauge $e^0{}_i = 0$ | always ($H^3$ contractible) | free |
| …for $O(3,1)$ rather than $SO^+(3,1)$ | $M$ time-orientable | $H^1(M;\mathbb Z_2)$ |
| Weitzenböck $\omega = 0$ | $M$ parallelizable $\iff w_2 = 0$ (Geroch) | spin structure |
| unitary gauge (Goldstone section) | $\det e\ne0$ region only | **runtime**, §7.2 |

- **Outer-twisted transitions.** If face transitions in $\hat G$ with $\hat G/G = \Gamma\subset\mathrm{Out}(G)$ are permitted, the decoration's class lives in $H^1(\mathcal D;\Gamma)$ — and the diamond complex *is* the Čech cover. Time-orientability then becomes a runtime readout of the decoration rather than a declared input. Untwisted, an outer identification is a redundancy of labelling; twisted, it is physical monodromy.

### 9.5 The split ambiguity — must be declared

Under $\mathfrak h = \mathfrak{so}(3)$,

$$\mathfrak{iso}(3,1) = \mathbf 1_{P_0}\oplus\mathbf 3_{P}\oplus\mathbf 3_{K}\oplus\mathbf 3_{J},$$

three copies of $\mathbf 3$. The $\mathrm{Ad}(SO(3))$-invariant complement to $\mathfrak h$ is therefore **not unique**: there is a two-parameter family

$$P_i\mapsto P_i + a\,J_i,\qquad K_i\mapsto K_i + b\,J_i,$$

all giving legitimate principal $SO(3)$-connections. At $\mathfrak h = \mathfrak{so}(3,1)$ the ambiguity does not exist — adjoint and vector are inequivalent, multiplicity one, split canonical. **Reducing to $SO(3)$ creates it.**

SI kills part of it: $[P] = \mathrm m^{-1}$ while $J,K$ are dimensionless, so $a$ requires a length scale (supplied in this specification by $\ell_0$ or $D_A$), but $b$ is dimensionless and genuinely free.

This is the same species of freedom that distinguishes ADM from BSSN from Z4 — rearrangements agreeing in the continuum and differing in principal symbol, which §12.1 identifies as what decides whether the algebroid residual stays soft. So it is a knob, not a nuisance. But **nothing in the dynamics pins it**, and a 2-parameter sloshing between what is called connection and what is called coframe will present as unreproducible hyperbolicity between implementations. Declare $a,b$ in §6.4 and record them with results.

---

## 10. Interior treatments

All three options are the same construction — a timelike tube with flux accounting on its boundary — differing in how much interior physics is retained.

### 10.1 Why something is needed

Collapse is unbounded (§4.6). Fixed node count cannot follow it.

### 10.2 Monotone refinement, and sinks

**Monotone refinement is free.** With nested spaces $Q_j\subset Q_{j+1} = Q_j\oplus W_j$ (hierarchical FEM, wavelets), initialising new modes at $(q_W,p_W) = (0,0)$ is an exact symplectic embedding: exactly canonical, exactly energy-preserving. If you only ever add and never remove, structure is preserved exactly. The state grows; nothing is lost.

**Coarsening is not free.** Deleting a mode removes nonzero energy; the reduced dynamics is Markovian term + memory kernel + noise (Mori–Zwanzig), and coarse-graining a Hamiltonian system is provably non-Hamiltonian.

**Sinks.** Where the interior need not be resolved, replace it with a low-dimensional effective object carrying mass, momentum, spin, and an entropy variable, in GENERIC form:

$$\dot u = L\,\frac{\delta E}{\delta u} + M\,\frac{\delta S}{\delta u},\qquad L\,\frac{\delta S}{\delta u} = 0,\quad M\,\frac{\delta E}{\delta u} = 0 .$$

The degeneracy conditions give exactly conserved total energy and monotone entropy production. Discarded energy is *accounted*, not leaked, and $\dot S\ge0$ is a first-class diagnostic. Trigger on Jeans violation, boundedness, and convergence — well before horizon scale.

### 10.3 Excision

**Requires the §2.1 declarations of cosmic censorship and the NEC.** Without them this section is unjustified.

**Use apparent horizons, not event horizons.** The event horizon is $\partial J^-(\mathcal I^+)$: global and teleological, requiring the entire future, growing in anticipation of infalling matter. It cannot be a runtime criterion. The apparent horizon — outermost marginally outer trapped surface, $\theta_{(\ell)} = D_is^i + K_{ij}s^is^j - K = 0$ — is computable from data on a single slice.

Under censorship and the NEC, $\mathcal{AH}\subseteq\mathcal{EH}$, so excising inside $\mathcal{AH}$ is causally safe: nothing there reaches any exterior observer. Two properties matter:

- The excision boundary is **causally outflow** — all characteristics leave the domain, so no boundary condition is required. This is why the technique is stable and why it injects no reflections that a dissipation-free scheme would conserve forever.
- Excision removes the *driver* of slice stretching, so §5.3 is eliminated rather than deferred. This, not degree-of-freedom count, is the main reason to excise.

**Closure.** The principled version is the dynamical-horizon flux law (Ashtekar–Krishnan): the horizon 3-surface is spacelike when accreting, null when isolated, and satisfies an exact balance of $\Delta A\,c^4/16\pi G$ against matter energy and shear flux. Isolated-horizon boundary conditions give a first law with locally defined $M$ and $J$. (Those sources are geometrised; the $c^4/G$ is restored here.) This is the GENERIC reservoir with the bonus that closure error is causally shielded rather than merely small.

**Caveats.**
- Excision is directional: it removes outward influence, not inward. Accretion, tidal response, and horizon growth still couple in.
- MOTS are foliation-dependent — there exist slices of Schwarzschild carrying no trapped surfaces. Since the monitor drives the lapse, **keep the slicing horizon-adapted where excision occurs** rather than letting the monitor drive it there.
- Horizons appear only at $\rho\sim c^6/G^3M^2$, far above the densities where the resolution problem begins. Excision caps the tail; sinks handle the body.
- Moving the excision surface through the grid is where most instabilities in this technique originate. Moving punctures (1+log slicing, Gamma-driver shift) avoid the surface entirely and are the recommended default — and are themselves an instance of a metric-driven mesh, i.e. of this specification's own principle.
- An excised hole still lenses. Keep $D_A$ integration and multipole matching outside the boundary.

### 10.4 Distant interiors are cheap

An observer at $D_A$ from a clump of size $a$ resolves internal structure only up to multipole $\ell_{\max}\approx a/\theta D_A$ — the Barnes–Hut opening criterion, read as a statement about effective dynamics. A distant clump's interior need only be converged in the multipoles that reach anyone. Track $\ell_{\max}$ per excised region per observer; this is what distinguishes an honest excision from one quietly corrupting somebody's sky.

The caveat is chaos: interior errors can climb to low multipoles (a binary's orbital phase; whether a clump fragments into one object or three). Where that happens, §14.1 applies.

---

## 11. Observers and output

### 11.1 Light-cone output

No observer measures a simultaneity slice; they measure their past light cone, which is invariant. Simultaneity is a reconstruction, never a datum.

The consequence is favourable. The state is $3+1$ dimensional; what reaches an observer at one moment is a 2-sphere, $\sim\theta^{-2}$ pixels — consistent with the corrected cost law of §4.6, and the same $S^2$ that carries the conformal structure of §4.3. Write cells as they cross $\partial J^-(p_k)$ and discard the rest. This crossing is the handover
to the render module and the point at which working precision is converted down (§2.5); the
integrator's arithmetic is invisible beyond it. Each spatial location crosses each observer's cone once, so cost is $O(\mathcal N)$ per observer **for the whole run**, not per step. Standard practice in cosmological mock-catalogue generation.

### 11.2 Region of interest

$\bigcup_k J^-(p_k)$ over observer events is closed under taking pasts, hence self-consistent: nothing outside it is needed to compute anything inside it. Evaluation can be demand-driven — a cell is computed when it is about to enter someone's causal past. The complement may be coarsened aggressively regardless of horizons, and this is a larger saving than black-hole excision and requires no censorship assumption to justify.

For prescribed worldlines this is exact. For dynamical observers, an observer cannot leave their own light cone, so $J^-(\text{worldline})\subseteq J^-(q)$ for any $q$ in their causal future: compute the enlarged cone with margin.

### 11.3 Storage

Lorentzian signature does **not** break the Cauchy problem — evolution is Markovian in whatever foliation is chosen, and frame-dependence of simultaneity is gauge. The genuine requirement is a rolling buffer one light-crossing time deep, since a slice boosted at $v$ through a region of size $L$ skews by $\Delta t\sim vL/c^2\le L/c$. A fixed multiplier, not a growing one. Under hyperboloidal compactification the outer slices are already asymptotically null and hence light-cone-adapted, so required buffer depth in $\tau$ stays bounded as $r\to\infty$.

Note that $v/c$ here is also the distance from the Galilean orbit boundary (§2.2): the fibre $H^3$ has curvature radius set by $c$, and the Galilean contraction flattens it to $\mathbb R^3$. The buffer depth and the stiffness margin are the same quantity.

### 11.4 Retroactive horizon finding

If the larger excision region is wanted, terminate at $T$, integrate null generators backwards, and obtain the truncated horizon $\mathcal H_T$ with $\mathcal{AH}_t\subseteq\mathcal H_T\subseteq\mathcal{EH}$, converging as $e^{-\kappa(T-t)/c}$ with $\kappa$ the surface gravity. A few light-crossing times of lookahead suffices.

This is a *post-processing* operation: it makes the state at $t$ depend on data at $T > t$, so the map is not autonomous and the generating-function argument does not cover it. Keep it off the forward pass. $\mathcal H_T$ under-estimates when matter falls in after $T$, so lookahead must exceed the accretion timescale, not merely $\kappa^{-1}$.

**Cosmological horizons are a different node.** $\mathcal I^+$ is spacelike in de Sitter and no finite lookahead reaches it. §11.4 and §14 discussing de Sitter are at $(\mathfrak{so}(1,4),SO(3))$ — observer space $dS_4\times H^3$ — not at $\mathfrak{iso}(3,1)$. **The document straddles two nodes and the dispatch differs between them.** Two consequences:

1. The relevant tool is the cosmic no-hair theorem, which makes the asymptotic answer decidable from the attractor structure without integrating to it.
2. At the de Sitter node the model curvature is nonzero and **well-balancing is live**: store $F_{\mathfrak h} = R^{ab} - \ell^{-2}e^a\wedge e^b$, the Cartan curvature relative to the model, not $R$ and $\ell^{-2}e\wedge e$ separately. With $\ell = \sqrt{3/\Lambda}\approx1.6\times10^{26}\,\mathrm m$ against simulation scale $L$, the avoided cancellation is $(\ell/L)^2$ — the same quantity the classifier reports as $d_{\rm contr}$. At the $\mathfrak{iso}(3,1)$ node $\ell = \infty$, the model curvature vanishes, and this reduces to the flux form §7.1 already has: **no benefit, do not implement it there.**

---

## 12. Parallelisation

### 12.1 Confluence is causality

The discrete spacetime is a poset and its partial order **is** the data-dependency graph. Spacelike-separated updates commute exactly, so any linear extension of the causal order yields the same state. Functorially: evolution is a functor from causally convex regions (ordered by inclusion) to $\mathbf{Symp}$, with $\Phi_{U\to W} = \Phi_{V\to W}\circ\Phi_{U\to V}$. Slice-independence and schedule-independence are the same theorem; the parallel scheduler is the constructive content of Tomonaga–Schwinger.

*Numerics note.* "Confluence" is the property that the result does not depend on the order in which independent work is done.

**Corollary.** The $O(h^p)$ failure of the discrete hypersurface deformation algebra to close is a *race condition*. Because that algebra is a Lie algebroid with structure functions $\gamma^{ij}$ rather than a Lie algebra with structure constants, there is no finite-dimensional gauge group to preserve exactly, and the residual is an anomaly whose soft/hard character is decided by the principal symbol of the constrained system — which is why BSSN and Z4 differ in stability despite continuum equivalence, and why §9.5's $(a,b)$ matter. Since the monitor drives the lapse, it can move the scheme between those regimes. Measuring foliation-dependence and measuring non-confluence are one diagnostic.

**This is what forces §3.3's causal selection rule.** $H_D$ chosen from any non-schedule-invariant data breaks the functor.

### 12.2 No global barrier

*Numerics note.* A "barrier" is a synchronisation point where every process must arrive before any proceeds. One per step destroys asynchrony.

| Global (barrier) | Local (parallel) | Already chosen because |
|---|---|---|
| elliptic equidistribution | mesh inertia (§6.1) | structure preservation |
| maximal slicing, minimal distortion | 1+log, Gamma-driver | hyperbolic gauge, no coordinate shocks |
| constraint projection | Z4c/CCZ4 damping | variational compatibility |
| instantaneous Poisson (Newton–Cartan) | hyperbolic GR | finite $c$, causal shielding |

Every choice made for geometric reasons is also the parallel-friendly one. Not coincidence: locality of the field equations is what both structure preservation and scalability purchase. See §2.3 for the full Newton–Cartan argument, now stated as the applicability gate.

### 12.3 Scheduling

**Decompose in $\mathcal C$, not $\Sigma$.** Every node takes exactly one update per $\Delta\tau$ by construction, so load is uniform in $\xi$ even though physical resolution varies by orders of magnitude. The fictitious-time formulation adopted for Ge–Marsden reasons converts the hardest problem in adaptive parallel codes into a non-problem.

*Numerics note.* A "halo exchange" is the refresh of each process's copy of its neighbours' boundary cells.

**Diamond tiling.** Compute a spacetime tile whose spatial footprint shrinks each substep — the numerical domain of dependence — so $k$ steps cost one exchange of depth $k$. The tile is a causal diamond $J^+(p)\cap J^-(q)$; communication drops by $k$ at the cost of redundant work in the overlap. Multisymplecticity is per-cell and survives arbitrary tiling exactly. The tiles are the natural conservation cells of §13, **and the carriers of the stratum label of §9** — one object, three roles.

**Synchronisation.** Conservative scheduling (advance only when no earlier message can arrive) requires nonzero *lookahead* to avoid deadlock, and the stability condition supplies exactly that: a neighbour cannot influence a cell for at least $\Delta x/c$. Finite light speed gives free lookahead, making conservative scheduling unusually well-suited. Optimistic scheduling (advance speculatively, roll back) is also cheap, because a palindromic symplectic integrator satisfies $\Phi_h^{-1} = \Phi_{-h}$ and can *reverse-compute* instead of storing checkpoints — the same reversibility used in §11.4.

### 12.4 Determinism

Floating-point addition is not associative, so a different reduction order changes the last bits and chaos amplifies that to $O(1)$. Asynchronous runs will not reproduce bitwise unless reduction order is fixed. **Fixed-point arithmetic** buys determinism and exact reversal together, the latter regardless of Lyapunov exponent (Levesque–Verlet). Recommended.

---

## 13. Conservation and diagnostics

### 13.1 Diamond residual — the primary diagnostic

Global energy is not definable without a consistent Cauchy surface through an asynchronous mesh, and in GR the ADM bulk term vanishes on the constraint surface anyway: energy is already purely a boundary term. Replace the global measurement with

$$\mathcal R(D) = \oint_{\partial D}\star J \;\overset{!}{=}\; O(h^p), \qquad J^\mu = T^{\mu\nu}\xi_\nu ,\qquad J\in\mathfrak g^* .$$

For a Killing $\xi$, $\nabla_\mu J^\mu = 0$ and the balance is exact. Without a Killing vector there is no covariant integral law — parallel transport is path-dependent — and what survives is quasi-local: Brown–York, Hayward, or the dynamical-horizon flux law. Gravitational energy has no local flux at all; pseudotensors are coordinate artifacts.

**$\mathfrak g^*$-valued, not $\mathfrak h^*$-valued** (§7.1). This is what makes the residual stratum-independent and therefore usable across §9's decoration. Project to the local $\mathfrak h$ for display only.

The residual is strictly better than a global band: foliation-free, schedule-independent, computable per tile, and it *localises* failures. Sample over test diamonds at several scales; $h^p$ scaling confirms order, and any failing diamond flags a face where flux uniqueness broke. Place test diamonds straddling every $m_K$ interface **and every stratum interface**.

### 13.2 $T^{\mu\nu}$ symmetry

In SI with $x^0 = ct$: energy flux $S^i = c\,T^{0i}$ and momentum density $g^i = T^{i0}/c$, so symmetry gives $S^i = c^2g^i$. One face flux tensor, contracted with different $\xi$, yields energy, momentum, and angular momentum — no separate machinery.

**Trap.** Symmetry of $T^{\mu\nu}$ is what makes angular momentum conserve with the *same* fluxes (Belinfante–Rosenfeld). Staggered discretisations routinely break it: storing $T^{0i}$ and $T^{i0}$ at different grid locations makes them different numbers, and angular momentum then leaks while energy and linear momentum remain exact. Collocate the off-diagonal components or symmetrise explicitly.

### 13.3 Diagnostic set

| Quantity | Meaning | Action on failure |
|---|---|---|
| $\min J$ | mesh tangling | raise $\lambda_v$; alarm |
| $\max c\|\nabla T\|$ | slice tilt | now bounded structurally (§5.3); alarm indicates an RKMK bug |
| $\max K$ | cell distortion | raise $\lambda_s$; go curl-free |
| per-cell grading ratio | reflection risk | target $\lesssim1.15$ |
| **per-face $\Delta\eta$** | frame-grading reflection | target $\lesssim0.14$ (§7.5) |
| **cocycle closure** | spurious discrete torsion | §13.4 — localise the offending loop |
| $\lambda_J/\Delta x$ | validity floor | **hard alarm**, not a warning |
| $\mathcal R(D)$, $\mathfrak g^*$-valued | conservation | localise the offending face |
| momentum-map residual, $\mathfrak g^*$-valued | symmetry preservation | check interface stationarity |
| constraint-algebra residual | foliation-dependence / anomaly | compare schedules; check $(a,b)$ |
| confluence test | race conditions | two schedules, compare |
| $m_K$ crossing **coherence** | chatter drift | stagger; widen hysteresis |
| $\dot S\ge0$ | reservoir honesty | sink closure is leaking |
| $\ell_{\max}$ per excision per observer | is the excision visible? | shrink region or resolve |
| $\lambda(\tau)$ history | budget pressure | report with results |
| $m^\star/m$ | polaron drag | lower $\mu$; renormalise |
| $Q$ (integer) | topological charge | degenerate-triangle check |
| **classifier tuple + $(a,b)$ + $\ell_0$ + $d$ + matter content + precision/representation** | run identity | record in every log header (§17.6, §2.6, §2.7) |
| **operator interventions** | non-autonomous forcing | reset conservation baselines at each; §13.1 will otherwise read a gesture as a failure |
| wave amplitude vs constraint residual | physical radiation or junk | §17.4 — gate on this before trusting rate dependence |

The confluence test deserves emphasis: running two schedules and comparing is simultaneously a race-condition check and a measurement of the constraint-algebra anomaly. It is the cheapest high-value diagnostic in the set, and it is the gate on enabling §9.

The last row is not decoration. Reproducing a result requires knowing which node the run was on; "Lorentzian, reductive, $\dim\mathfrak h = 3$, $d_{\rm contr}$" plus $(a,b,\ell_0)$ is seven numbers.

### 13.4 Cocycle closure

New in Revision 2. Around any closed loop of faces in the diamond complex, the composed chart transitions must equal the physical holonomy of $A$ on that loop:

$$\prod_{\text{faces}\in\partial\Box} g_{\text{face}} \;\overset{!}{=}\; \mathcal P\exp\oint_{\partial\Box} A .$$

Any discrepancy is spurious discrete torsion introduced by the decoration. This is a Čech condition on the 2-skeleton, cheap, and it localises exactly like §13.1. Run it wherever strata meet.

---

## 14. Limits

Properties of the problem, not deficiencies of the method. State them in any results derived from it.

### 14.1 Chaos and shadowing

Errors grow $\sim e^{\lambda t}$; no finite-precision method tracks a specific trajectory over cosmological times. The correct substitute is the shadowing lemma: on a hyperbolic set, the computed orbit is the *exact* orbit of slightly perturbed initial data. The claim is not "this is the universe" but "this is exactly *a* universe, indistinguishable at the stated resolution." For clustering, spectra, and correlation functions that is the physically meaningful notion, and preserving the symplectic structure is precisely what makes it trustworthy — strictly stronger than small-$h$ accuracy.

### 14.2 Accuracy horizon

What reaches an observer at time $T$ left radius $cT$ at $t = 0$. Just-in-time refinement cannot repair error already committed in the far field, and with no dissipation nothing is forgotten. Budget resolution retroactively along past light cones; the horizon grows at $c$.

### 14.3 Information

Coarse-graining is lossy by construction: Mori–Zwanzig memory is nonlocal in time and generically not representable by any finite local state. A simulation with fewer degrees of freedom than its target cannot be exact, at any step size, with any integrator. $r$-adaptivity moves resolution; it does not create it.

### 14.4 Residual discreteness

One *moving* discontinuity survives the design: $m_K\in\mathbb Z$. It is mild (canonical change of scheme, no lost degrees of freedom, no memory kernel) but real, and hysteresis is mandatory, not advisory. The stratum label of §9 is also discrete but static by §9.2 and therefore contributes nothing here.

### 14.5 What the classifier cannot certify

Signature gives a **necessary** condition for a well-posed IVP, never a sufficient one. Strong or symmetric hyperbolicity is a property of the *formulation and gauge*, not of $(\mathfrak g,\mathfrak h)$: ADM is only weakly hyperbolic, BSSN and Z4c are strongly hyperbolic, and both describe the same geometry. The classifier must emit "hyperbolic PDE class; hyperbolicity of the chosen formulation unverified" and the principal-symbol check belongs in the formulation module. §9.5's $(a,b)$ live exactly in that gap.

---

## 15. Implementation order

0. **Classifier and gate.** Declare $(G,H_{\max})$, topology, asymptotics, assumptions — as configuration keys per §2.5. Emit the tuple. Verify effectiveness (§9.4). Fail loudly outside Lorentzian, by throwing from `onInitialize()` so the engine propagates it (R22) rather than continuing with a partially initialised module set.
1. **Fixed-step, uniform mesh, flux form**, $\mathfrak g^*$-valued fluxes from the start. Verify telescoping, energy band, momentum maps. No adaptivity.
2. **Static graded mesh.** Verify grading ratio and reflection; confirm the band widens as predicted with the coarsest region.
3. **Dynamical mesh, prescribed monitor.** Inertia; volume barrier via exact splitting (§7.2); polar-decomposition interpolation. Tune $\mu/\epsilon$ to critical damping. Verify $\min J$, $\max K$.
4. **Monitor from field gradients.** Smoothing and $r_{\max}$. Watch the compression feedback of §6.2.
5. **Lapse from Jacobian with $\ell_0$ declared; RKMK section stepping on $H^3$.** Verify the §5.3 bound empirically on a stationary compression; confirm spacelikeness is unconditional and that no $\lambda_t$ is needed.
6. **Observers.** Hyperboloidal chart, $D_A$ integration, light-cone output, celestial-sphere bookkeeping. Verify the corrected §4.6 cost law. Verify $m^\star/m$ if dynamical.
7. **Subcycling.** $m_K$ powers of two, hysteresis, buffered grading, coherence diagnostic. Diamond residuals across interfaces.
8. **Parallel.** Decompose in $\mathcal C$; diamond tiling; conservative scheduling on CFL lookahead. **Confluence test must pass before step 9.**
9. **Stratification.** Static labels only. Cocycle closure. $\Delta\eta$ grading. IMEX at interfaces. Start with two strata and a single flat interface.
10. **Sinks with GENERIC reservoir.** Verify $\dot S\ge0$.
11. **Excision.** MOTS finder, horizon-adapted slicing locally, moving punctures. $\ell_{\max}$ monitoring.
12. **Fixed-point arithmetic** if determinism or exact rollback is needed.
13. **Dynamical gravity last**, with the fiducial split, $(a,b)$ declared, and only after the mesh sector's characteristic analysis is done. Use the §7.1 first-order primitives so this is the same code path as §7.3.

Steps 1–8 are rigorous and use deployed techniques. Step 9 is Cauchy–characteristic matching in new clothing — deployed, but its interface is the hard part. Step 10's closure is thermodynamically sound but problem-specific. Steps 11–13 in combination are research-grade.

---

## 16. Open problems

1. **Well-posedness of the coupled system.** The mesh is a dynamical field with its own kinetic term and characteristic speeds. Strong hyperbolicity of field + mesh + gauge together has not been established for this action and should be checked per problem class. Plausible structural reason it is hard: by §4.1 the coupled system's natural home is $\mathcal O^7$, since the monitor depends on $u$ and the curvature therefore has boost legs — $\iota_{\hat X}F\ne0$, so the system is genuinely outside the image of the reduction functor and is being written in 4-dimensional variables.
2. **Ghost-freedom of $\mathcal P$** on a dynamical metric. The fiducial split removes the source but not the mesh's own propagation (§3.4). Default to the split until settled.
3. **Fixing $(a,b)$.** Nothing in the dynamics pins the split (§9.5), and the choice moves the principal symbol. Is there a normality-type condition selecting it, analogous to the parabolic case where $\partial^*$ makes the choice canonical?
4. **Stability theory for AVI patches** — no clean CFL-type bound exists.
5. **Sink closure.** The memory kernel is problem-specific; this is where physics judgement, not formalism, determines quality.
6. **Anomaly classification.** Whether a given monitor-driven lapse keeps the discrete algebroid residual soft is currently empirical.
7. **Multi-stratum interfaces.** §9.3 covers a single interface. Triple junctions, and whether the cocycle condition of §13.4 is sufficient there, are untested.

---

## 17. Interactive operation

New in this draft. Sections 1–16 assume batch evolution from declared initial data. This section
governs the case where a human is in the loop — moving an observer, editing the state, and
looking at the result while it runs. It is not a UI chapter: interactivity touches the gauge
structure, and getting it wrong means silently solving a different theory.

### 17.1 Observers break the gauged symmetry — the distinction that matters

$\mathfrak g = \mathfrak{iso}(3,1)$ is *gauged*. A point-particle observer picks out a location
(breaking translations), a four-velocity (breaking boosts), and a celestial orientation (breaking
rotations). The monitor of §4 is built from exactly that data, and by §6.2 it enters the action
through $w[\phi] = \sqrt{\det M}$. **The observer is therefore in the Lagrangian, not in the
bookkeeping.** §16.1 already names the mechanism — the monitor depends on $u$, so
$\iota_{\hat X}F\ne0$ and the system's natural home is $\mathcal O^7$ — but files it as an
obstacle to proving well-posedness rather than as symmetry breaking.

The severity is decided by *which kind* of breaking it is:

| Observer treatment | Breaking | Consequence |
|---|---|---|
| **Prescribed** worldline, fixed $\theta$ | **explicit** — $L$ itself is not invariant | solving a modified theory; results are not GR |
| **Dynamical**, $q_{\rm obs}\in T^*Q$ | **spontaneous** — $L$ invariant, the state selects a solution | benign; any matter distribution does this |

§5.5 already offers both without noting they differ in kind. **They differ in kind.** A detector in
a real spacetime breaks translation invariance too; that is not a defect of the theory, it is
having something in the universe. A prescribed observer is a different animal — it is an external
structure in $L$, and no amount of numerical care recovers what it removed.

Acuity must be carried the same way. A $\theta$ fixed in some external frame breaks Lorentz
explicitly; a $\theta$ carried by the observer and transforming under aberration does not. §4.3
already supplies the transformation law — $\theta$ is a Weyl structure on $\partial H^3\cong S^2$
with $SO^+(3,1)$ acting by Möbius transformations and conformal factor $\mathcal D$. Use it
rather than assuming it away. $\lambda(\tau)$ is a global scalar and is invariant either way.

### 17.2 Input must be expressed in the observer's own frame

Making the observer dynamical is not sufficient, because a controlled observer carries an applied
force, and a force specified in an external frame reintroduces explicit breaking. The escape is
that it need not be:

$$\boxed{\;f^\mu = a^i\,e_i{}^\mu(q_{\rm obs})\;}$$

with $e_i{}^\mu$ the observer's own spatial triad and $a^i$ the input. Both factors are built from
fields already in the theory, so $f^\mu$ is a genuine spacetime vector and nothing external picks
a direction. This is why a rocket is covariant and a "move to $(x,y,z)$" command is not: thrust is
defined by the vehicle's own axis, a coordinate target presupposes a chart.

For a first-person interface this is automatic, because every input device is already attached to
the viewpoint:

| Input | Covariant because |
|---|---|
| Thrust | components in the comoving frame, contracted with the observer's triad |
| Look | rotation within the observer's own little group, acting on its tetrad (§9.1's $SO(3)$ node) |
| Point / click | a direction on the observer's celestial sphere — **a pixel is already a covariant object** (§4.3), transforming by aberration |
| Thrust magnitude, $\theta$, $\lambda$ | scalars |

**The corresponding prohibition is a hard interface constraint, not a preference.** Any control
referencing a global frame reintroduces explicit breaking: "go to the origin", "align to the
$x$-axis", a minimap with fixed axes, a typed coordinate entry. Cheap to honour from the start,
expensive to retrofit.

**What survives** is that input is a function of wall-clock time, so $H$ is non-autonomous. That
costs energy conservation — not symplecticity, and not gauge invariance. §5.5 states the same
condition for prescribed worldlines ($\tau_\rho\ll c/a$), and §10.2's reservoir is the right place
to account for the work done, which also yields the honest figure for how much energy the operator
injected.

### 17.3 Editing the state

The constraints couple matter to geometry:

$$R + K^2 - K_{ij}K^{ij} = \frac{16\pi G}{c^4}\rho ,$$

so $\rho$ cannot be edited with $\gamma_{ij},K_{ij}$ held fixed. This is the sharp difference from a
Newtonian sandbox, where $\Phi$ is algebraically slaved to $\rho$ and *any* configuration is valid
initial data. Here the geometry carries its own degrees of freedom plus four constraints, and an
arbitrary edit lands off the constraint surface.

Three treatments, in increasing order of honesty:

| Treatment | Constraint handling | Verdict |
|---|---|---|
| **Edit and damp** | violate, then relax with Z4c/CCZ4 | works, but the visible transient is largely *constraint-violating junk*, which NR discards as such |
| **Quasi-static along the constraint surface** | evolve along $\mathcal C$ as matter is added — a **linear** elliptic solve per step, warm-started between frames | energy injected is the path integral, hence accounted; barrier confined to the gesture |
| **Force on matter** | none needed — matter accelerates under $f^\mu$ and geometry responds through the evolution equations | **preferred**; constraints never break, no elliptic solve at all |

The third is the same construction as §17.2 applied to matter rather than to the observer, and it
is preferred for a reason beyond cleanliness — see §17.4.

Notes on the second. The constraint surface is the zero level set of the momentum map for the
hypersurface-deformation group, so "stay on $\mathcal C$" is an invariant §7.1 and §13.1 already
preserve, not a new one. The path is **not unique**, and different paths deposit different
gravitational radiation; canonicalise by moving orthogonally to the gauge orbits (minimal
distortion / conformal thin-sandwich) or the operator's gesture speed silently sets wave content.
The path also **terminates**: quasi-static sequences reach a turning point exactly as neutron-star
sequences do at maximum mass, beyond which no nearby constraint-satisfying configuration with more
matter exists. The correct behaviour there is that adding fails and collapse begins — real physics
falling out of the construction rather than a coded special case.

### 17.4 Rate dependence is physical, but only under one implementation

Radiated power goes as the square of the third time derivative of the quadrupole moment, so a fast
edit genuinely radiates and a slow one genuinely does not. This is the one thing a Newtonian
sandbox structurally cannot exhibit, and it is the clearest available demonstration that gravity
has radiative degrees of freedom and a finite propagation speed. It should be presented as a
feature.

**It is only genuine under force-on-matter.** Under edit-and-damp, what the operator sees after a
fast gesture is mostly the scheme relaxing onto $\mathcal C$ — junk radiation, monotone in the same
variable as the real effect and therefore persuasive while being numerics.

The discriminators are already in §13.3 and must be run before the behaviour is trusted:

- **Constraint residual correlation.** If wave amplitude tracks the constraint-algebra residual, it
  is junk. If constraints stay satisfied and waves appear regardless, they are physical.
- **Convergence under refinement.** Physical radiation converges as the mesh refines; junk does not
  converge to the same amplitude.

Two caveats survive even in the good case, and belong in any presentation of it: a hand grabbing a
star is not a physical process, so this is the *correct* response to a *fictional* force; and the
energy arrives from the §10.2 reservoir rather than from anywhere in the spacetime.

### 17.5 Trespass into invalid regions

A controlled observer can go where the simulation is not valid. Three kinds, with different
remedies:

| Kind | Meaning | Remedy |
|---|---|---|
| **Recoverable** | under-resolved, but §4.2's observer term will refine it | bounded by mesh response — see below |
| **Historical** | the state there was *computed* coarsely; §14.2 says that cannot be repaired | pre-emptive refinement of the reachable set |
| **Structural** | the data does not exist — sink interior (§10.2) or excised region (§10.3) | none; degrees of freedom are gone (§14.3) |

**Mesh inertia sets a speed limit.** §6.4 requires $\tau_\rho$ below $c/a$ for accelerating
observers. Read backwards, that bounds the operator:

$$\boxed{\;a \lesssim c/\tau_\rho\;}$$

Exceed it and the observer outruns its own resolution. This is the one restriction worth enforcing
rather than displaying, because exceeding it degrades the solution globally rather than locally —
and it has an honest physical reading, since no vehicle accelerates arbitrarily hard either.

**Refine the reachable set, not the light cone.** §11.2 refines $J^-$ of the observer's causal
future with margin. Under bounded acceleration the reachable set is far tighter than the full cone,
which makes pre-emptive refinement affordable. It must be pre-emptive; §14.2 admits no retroactive
version.

**Render invalidity rather than forbid trespass.** Every detector already exists — §13.3 flags
$\lambda_J/\Delta x$ as a *hard alarm*, §10.4 tracks $\ell_{\max}$ per excised region per observer
precisely to distinguish "an honest excision from one quietly corrupting somebody's sky", and
§13.1's $\mathcal R(D)$ localises per diamond. Surfacing them costs nothing that is not already
computed. An under-resolved region should visibly degrade; a sink interior should look like what it
is — mass, momentum and spin without structure — rather than like plausible detail. A simulator
that shows where it stops being trustworthy is worth more than one that always looks convincing.

### 17.6 Precision is an operating mode

Interactive and rigorous operation want different arithmetic, and §0's conditioning warning and
§11.4's well-balancing are both workarounds for 53-bit mantissas. Make precision a **declared
input** (§2.1), uniform over the run and selected from implemented backends rather than a free
width — a configuration key subject to R31–R37, so an unimplemented width fails at load with a
named message rather than silently rounding to something nearby (R34). Uniform is what keeps §1 satisfied: a constant is trivially a smooth function of phase
space, whereas precision driven by a measured error estimate is precisely what §1 forbids. Static
per-region precision is also admissible, by §9.2's argument — a discrete field is inadmissible only
when it *moves*.

Two consequences. Running the same problem at two precisions separates roundoff from truncation
empirically, which sharpens every §13 diagnostic. And precision must appear in the run header
alongside the classifier tuple, since two runs at different precision are not comparable and a
53-bit-only defect is invisible in a high-precision reference.

Note that precision and *representation* are orthogonal: more bits does not make floating-point
addition associative, so §12.4's bitwise determinism and exact rollback still require fixed point.

---

## 18. Key references

**Cartan geometry and classification.** Sharpe, *Differential Geometry: Cartan's Generalization of Klein's Erlangen Program* (signature-free). Čap & Slovák, *Parabolic Geometries I* — normality, the Kostant codifferential $\partial^*$, Weyl structures as an affine space. Stelle & West, PRD 21, 1466 (1980) — the Goldstone section; mostly-plus. MacDowell & Mansouri, PRL 38, 739 (1977); mostly-plus. Gielen & Wise on observer space. Patera, Winternitz & Zassenhaus on subalgebra classification. Figueroa-O'Farrill & Prohazka on kinematical Lie algebras.

**Geometric integration.** Hairer, Lubich & Wanner, *Geometric Numerical Integration* (2nd ed.) — backward error analysis, splitting, adaptivity obstructions; dimensionless throughout. Marsden & West, *Discrete Mechanics and Variational Integrators*, Acta Numerica 10 (2001). Leimkuhler & Reich, *Simulating Hamiltonian Dynamics*. Iserles, Munthe-Kaas, Nørsett & Zanna on Lie-group methods (RKMK).

**Adaptive and asynchronous.** Kane, Marsden & Ortiz (1999) — energy-conserving variable-step variational integrators. Lew, Marsden, Ortiz & West (2003) — AVIs. Hairer & Söderlind (2005) — reversible adaptivity. Calvo & Sanz-Serna (1993) — the obstruction.

**Multisymplectic.** Marsden, Patrick & Shkoller (1998). Bridges & Reich (2001).

**Numerical relativity.** Baumgarte & Shapiro, *Numerical Relativity*; Alcubierre, *Introduction to 3+1 Numerical Relativity*. Both geometrised. Ashtekar & Krishnan, *Isolated and Dynamical Horizons* (Living Rev. Rel., 2004). Winicour, *Characteristic Evolution and Matching* (Living Rev. Rel.) — the CCM experience §9.3 draws on. Zenginoğlu on hyperboloidal compactification.

**Mesh adaptivity.** Huang & Russell, *Adaptive Moving Mesh Methods*. Brenier (1991); Dacorogna & Moser (1990).

**Structure and thermodynamics.** Öttinger, *Beyond Equilibrium Thermodynamics* (GENERIC). Chorin, Hald & Kupferman on Mori–Zwanzig.

**Parallel.** Frigo & Strumpen on cache-oblivious stencils (diamond tiling). Fujimoto on conservative and optimistic synchronisation.

---

## Appendix A. Changes from Revision 1

**Corrections.**

| § | Revision 1 | Revision 2 |
|---|---|---|
| 5.1 | $N = \sigma J^{\beta/d}\Delta x^{1-\beta}/c$ with $\Delta x = J^{1/d}$ — collapses to $\sigma J^{1/d}/c$, so $\beta$ was inert while §5.3 and §6.4 treated it as live | $N = (\sigma/c)J^{\beta/d}\ell_0^{1-\beta}$ with $\ell_0$ declared |
| 4.6 | $\mathcal N_{\rm obs}\simeq(4\pi/\theta^3)\ln(R/r_0)$, presuming isotropic demand $\theta r$ | $\Pi^\perp$ is transverse-only; $\theta^{-2}$ per shell with the radial count from §5.4's height function. Scale-free survives, $\theta^{-3}$ does not |
| 4.1 | $\hat g = g + 2c^{-2}u\otimes u$, normalisation unstated | requires $u\cdot u = -c^2$; the $c^{-2}$ encodes it. Both forms given |
| 11.4 | well-balanced $F_{\mathfrak h}$ recommended generally | **empty at $\mathfrak{iso}(3,1)$** ($\ell=\infty$); live only at the de Sitter node. The document straddles two nodes |
| 6.2, 7.2 | log barrier "provably remains a diffeomorphism for all $\tau$" | continuum only; a finite step jumps it. Exact splitting, $GL^+$ geodesic step, or smooth guard |

**Additions.**

- §2 model layer: declared inputs, classifier tuple, applicability gate, internal-vs-external axes.
- §3.1 unreduced $\mathfrak g$-valued storage as the enabling decision.
- §3.3 decorated diamond complex and the causal selection rule $H_D = \mathcal H(J^-(D))$.
- §4.1 Riemannianisation identified as the $SO(3)$ reduction; contractibility of $H^3$ as the theorem behind global well-definedness; the fibre-orthogonality limit on multiple observers.
- §4.3, §4.5 acuity as a conformal structure on the celestial sphere; $\lambda(\tau)$ as a Weyl rescaling, which is why smooth degradation always exists.
- §5.3 tilt barrier replaced by RKMK stepping on $H^3$; $\lambda_t$ retired.
- §7.2 domains-are-not-constraints; polar-decomposition interpolation.
- §7.5 the $\Delta\eta\lesssim0.14$ frame-grading bound, unifying three gradings.
- §9 stratification: node menu, static-strata theorem, CCM interfaces, effectiveness and spin checks, the $(a,b)$ split ambiguity.
- §13.1 $\mathfrak g^*$-valued fluxes and momentum maps; §13.4 cocycle closure.
- §15 step 0 (classifier) and step 9 (stratification); §16 problems 3 and 7.

**Unchanged in substance.** §1 design principle, §5.2 slab generating function, §6.1 action and mesh inertia, §8 time stepping, §10 interior treatments, §11.1–11.3 observers, §12 parallelisation, §13.2 stress-tensor symmetry, §14.1–14.3 limits.

---

## Appendix B. Changes from Revision 2

**Corrections.**

| § | Revision 2 | Revision 3 |
|---|---|---|
| 5.5 | dynamical vs prescribed observer presented as a performance trade — back-reaction and polaron mass against non-autonomy | they differ *in kind*: a prescribed observer breaks the gauged Poincaré symmetry **explicitly** and so solves a modified theory; a dynamical one breaks it only **spontaneously**, as any matter distribution does. §17.1 |
| 16.1 | the monitor's dependence on $u$ filed as an obstacle to proving well-posedness | same mechanism, but the consequence stated: $\mathfrak g$ is gauged, so this *is* symmetry breaking, and its severity depends on how the observer is supplied. §17.1 |
| 0 | "implement in code units, convert on output", stated unconditionally | identified as a workaround for 53-bit mantissas; §17.6 makes precision declared, and at sufficient width SI becomes admissible for computation |
| 13.3 | run identity is classifier tuple $+\,(a,b)+\ell_0$ | precision and representation added — two runs at different precision are not comparable, and a 53-bit-only defect is invisible in a high-precision reference |

**Additions.**

- §17 interactive operation, in scope as of this revision and stated as such in §0.
- §17.1 the explicit/spontaneous distinction, and acuity as a quantity the observer must carry and transform by aberration rather than receive in a fixed frame.
- §17.2 covariance of applied force, $f^\mu = a^i e_i{}^\mu(q_{\rm obs})$, and the consequent prohibition on any control referencing a global frame.
- §17.3 the constraint coupling as the sharp difference from a Newtonian sandbox; three edit treatments ranked, with force-on-matter preferred; path non-uniqueness and the turning-point termination of quasi-static sequences.
- §17.4 rate dependence as a feature, valid only under force-on-matter, with constraint-residual correlation and convergence under refinement as the discriminators against junk radiation.
- §17.5 trespass into invalid regions, classified as recoverable, historical, and structural; the speed limit $a\lesssim c/\tau_\rho$ read out of §6.4's mesh response requirement; render-rather-than-forbid.
- §17.6 precision as a declared input and operating mode; why uniform precision satisfies §1 where an error-driven one would not; precision and representation as orthogonal axes.
- §13.3 rows for operator interventions (which otherwise read as conservation failures) and for the junk-versus-physical gate.

**Unchanged in substance.** §1 design principle, §2 model layer and applicability gate, §3 geometric setting, §4 monitor tensor, §5.1–§5.4 slicing, §6 action, §7 discretisation, §8 time stepping, §9 stratification, §10 interior treatments, §11 observers and output, §12 parallelisation, §13.1–§13.2 and §13.4 diagnostics, §14 limits, §15 implementation order, §16 open problems.

**Not yet reconciled.** §15's implementation order does not place the interactive work — §17.2's covariant input and §17.5's diagnostics are cheap and could sit near step 6, whereas §17.3's editing depends on how far §10 has progressed. §16 gains no new entry, though §17.4's junk-versus-physical determination is empirical in the same way problem 6 is.

**Also in Revision 3.** §2.5 maps this document onto the module system — declared inputs as configuration keys under R31–R37, the classifier gate as a `main` entrypoint failing through R22, light-cone output as a `SceneCallback` and as the precision conversion boundary, observer input as celestial-sphere directions from the render module's input interfaces, and §12's asynchrony as resting on R28. It also records the one active conflict with the existing engine: the renderer's clock and the integrator's clock are different clocks, so evolution stepping cannot live in a callback's `loop()`. Whether `Scene` is the diamond complex of §3.3 or merely owns one is left open there.

---

## Appendix C. Changes from Revision 3

**Corrections.**

| § | Revision 3 | Revision 4 |
|---|---|---|
| 2.1 | §17.6 instructed that precision be "a declared input (§2.1)", but no such row existed | precision and representation added to the table, along with dimension and matter content |
| 0 | signature $(-,+,+,+)$, Greek indices $0\ldots3$ — $3+1$ assumed silently, exactly the fault §2 was written to correct for the Klein pair | signature $(-,+,\ldots,+)$, indices $0\ldots d$, dimension declared; worked figures still quoted at $3+1$ but labelled as such |
| 3.1 | "$10\times4 = 40$ numbers per site" stated as a constant | $\dim\mathfrak g\times(d+1)$, tabulated across dimensions in §2.6 |
| 7.1 | primitives given by generator type, as two special cases | one rule — a field in representation $R$ is transported by $\rho_R(U_\ell)$ — of which those two are the adjoint and vector cases |
| 2.5 | the fixed-rate callback loop recorded as an unresolved conflict with this document | resolved: the engine carries **two** schedulers, fixed-rate for presentation and dynamic for evolution, with a second callback type for each |

**Additions.**

- §2.5 the applicability gate identified as the engine boundary: the engine supplies the model layer, classifier, gate and causal-type-agnostic machinery; the simulator is what runs once the gate passes. Nothing in the engine decides the spacetime. Notes the consequence that a Galilean node needs a second, barrier-based scheduler rather than a stub.
- §2.6 dimension: what generalises ($G$, the classifier tuple, $H^d$ contractibility, the celestial sphere) and what does not (little groups, storage, observer cost); $2+1$ as the validation target, including that Cartan there *is* Chern–Simons and that vacuum is locally flat, so local vacuum curvature measures error; the caveat that $2+1$ has no gravitational waves and therefore cannot exhibit §17.4; Kaluza–Klein recognised as §2.4's external quotient and the Eisenhart lift as a case where going up a dimension is the optimisation; edge cases at $1+1$, $2+1$ and $\geq4+1$ horizon topology.
- §2.7 matter content as a declared input: fields with their $G$-representations, the single transport rule, spinor declarations carrying a $w_2$ precondition into the gate, and §9.1's node menu identified as Wigner's classification read once for charts and once for available matter states.
- §9.1 the little groups generalised to $d+1$, and the double reading noted at the table itself.
- §13.3 run identity extended with $d$ and matter content.
- §2.5 the dynamic scheduler's contract: its tick interval **is** the lapse of §1 and §5.1; it must be a function of state, since the obvious implementation of a "when next?" method is error-driven and §1 forbids that outright; and it must be queryable without stepping, because §12.3's conservative scheduling needs lookahead before advancing rather than after. Notes that powers-of-two intervals give a nested-loop scheduler per §8.1, while arbitrary ones force a priority queue and interface interpolation.

**Unchanged in substance.** §1 design principle, §2.2–§2.4, §3–§8, §9.2–§9.5, §10–§16, §17 interactive operation.

**Not yet reconciled.** §15's implementation order does not mention exercising steps 1–8 at $2+1$ before $3+1$, which §2.6 recommends, nor does it place the dynamic scheduler — it belongs around step 7, where subcycling first needs per-cell clocks. §4 and §10 remain written for $3+1$ Lorentzian throughout — correctly, since they are simulator-side by §2.5, but the prose does not say so. §16 gains no entry, though two questions raised here have the same empirical character as the ones already listed: whether the Lorentzian and barrier-based schedulers share enough structure to be one implementation, and whether a monitor-derived lapse can be kept smooth enough in practice to satisfy §1 once it is exposed as an overridable method.
