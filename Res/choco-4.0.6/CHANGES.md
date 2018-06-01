Choco Solver ChangeLog
======================

This file is dedicated to sum up the new features added and bugs fixed in Choco-solver since the version, 4.0.0.
**Note**: double-space is replaced by "\t" character on release process. Make sure the format is ok.

4.0.6 - 23 Nov 2017
-------------------

Move to Ibex-2.6.3.

### Major features:
- Ibex instance is no longer static, that offers better stability and reduce
memory consumption when adding/removing functions. Reification no longer managed by Choco but 
delegated to Ibex. 
- `Search.realVarSearch(...)` offers possibility to define minimal range size, known as `epsilon`
- `Search.ibexSolving(model)` let Ibex iterates over solutions **once all integer variables are instantiated**
- add detection of min/max sub-cases
- add simple dashboard in Swing to show resolution statistics, see `solver.showDashboard()`

### Deprecated API (to be removed in next release):
- `IntEqRealConstraint` will be removed in next release, Ibex managed this concept (int to real)
- `Model.getIbex()` should not be used. A `IbexHandler` manages Ibex instances (one per model).

### Closed issues and pull requests:
\#558, #561, #565, #566, #568, #570

4.0.5 - 28 Sep 2017
-------------------

The current release was submitted to [MiniZinc Challenge 2017](http://www.minizinc.org/challenge2017/results2017.html) 
and at [XCSP3 Competition 2017](http://www.cril.univ-artois.fr/XCSP17/) and won medals.

[choco-parsers](https://github.com/chocoteam/choco-parsers) provides utility to export a `Model` to JSON format
and or import JSON data into a `Model`.

### Major features:
- Compact-Table now deals with short tuples (#531)
- Checking if a created constraint is free (neither posted or reified) is now possible with `Settings.checkDeclaredConstraints()`
- Improvements on BoolVarImpl and BoolNotView.
- Remove code deprecated in last release.
- Fix error in Views.
- Add scalar detection in `BiReExpression`
- fix errors in Impact-based Search
- update Search.intVarSearch() + Search.defaultSearch(Model)
- update ParallelPortfolio default strategies

### Deprecated API (to be removed in next release):

### Closed issues and pull requests:
- fix bug in `PropNogoods` when dealing with negative values (impact `solver..setNoGoodRecordingFromRestarts()` and `solver..setNoGoodRecordingFromSolutions(...)`)
- fix bug in `model.sum(...)` and `model.scalar(...)` when dealing with arity greater than 100 and all operators except `=`
- fix bug in `model.table(...)` with binary scope and universal value  
- fix bug related to Ibex and GC.

\#531 ,#545, #546.

4.0.4 - 28 Apr 2017
-------------------

### Major features:
- add logical operator to expression (#520). Now it is possible, f-ex., to declare expression like:
```x.eq(y.add(1)).or(x.eq(y).and(x.eq(1)))```
- add new API to `Solver` to print features in a single line
- enable ignoring passivate propagators when iterating over propagators of a modified variable (false by default; see Settings)

### Deprecated API (to be removed in next release):
- `IPropagationEngine.fails(c,v,m)` is replaced by `Solver.throwsException(c,v,m)` (#524)
- `IPropagationEngine.getContradictionException()` is replaced by `Solver.getContradictionException()` (#524)
- `MathUtils.bounds(values)` is replaced by a call to `MathUtils.min(values)` and `MathUtils.max(values)`

### Remove dead code:
- SparseSet
- IFeatures, Features, IAttribute and Attribute

### Closed issues and pull requests:

\#516, #517, #518, #519, #520, #521, #524.


4.0.3 - 31 Mar 2017
-------------------

### Major features:
- `arithm(IntVar,String,IntVar,String,int)` and `arithm(IntVar,String,IntVar,String,IntVar)` manage '*' and '/'
- add new APIs to `ArrayUtils`
- fix error in `PropBoolMin` and `PropBoolMax`

### Deprecated API:

### Closed issues and pull requests:

\#500, #502, #507, #510, #512, #514, #515.


4.0.2 - 20 Jan 2017
-------------------

### Major features:
  - restrict calls to `Solver.setEngine(...)` when propagation started. See javadoc for details.
  - remove global constructive disjunction, only local constructive disjunction is allowed.
  - add `Solution.restore()` to restore a solution (#354).
  - deep reset of `Solver` (#490, #491)
    
### Deprecated API:
  - `Solver.getState()` (#485)
  - `Measures.IN_SEC` (related to #486)
  - `Settings.outputWithANSIColors`, `IOutputFactory.ANSI_*`
  - `IMoveFactory.setLubyRestart(int, int, ICounter, int)`

### Closed issues and pull requests: 

\#468, #479, #480, #481, #484, #487, #488, #489, #492, #493, #494, #495, #496, #497, #499.

4.0.1 - 16 Dec 2016
-------------------


4.0.0 - 13 Sep 2016
-------------------
