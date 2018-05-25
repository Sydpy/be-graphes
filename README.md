# BE Graphes INSA Toulouse

## Auteurs

* Sylvain Dupouy
* Léo Picou

## Introduction

Ce bureau d'étude regroupe à la fois la pratique d'un langage orienté objet (Java) ainsi que l'implémentation d'algorithmes relevant de la théorie des graphes. Le projet initial est disponible [ici](https://gitea.typename.fr/INSA/be-graphes). Les fonctionnalités implémentées sont :
* L'algorithme de Dijkstra
* L'algorithme A* (en version Dijkstra)
* Un algorithme de résolution du problème d'échange de colis
* Des tests de validité concernant Dijkstra et A*
* Des tests de performance concernant Dijkstra et A*

## Conception

### Structures de données

### Dijkstra

`DijkstraAlgorithm` utilise des `Label` pour associer à chaque `Node` un coût. Ces `Label`s implémentent `Comparable<Label>` et on peu donc les ordonner en fonction de leur côut afin qu'à chaque itération, on récupère le `Label` (et donc le `Node`) de moindre coût non-traité. L'ordonnancement se fait via un tas binaire (classe `BinaryHeap`). Cette structure de données est particulièrement adaptée à Dijkstra puisque l'extraction de l'élément de coût minimal se fait en  compléxité O(log(n)).

Nous avons choisi de découper l'implémentation de l'algorithme en deux méthodes : `step()` et `doRun()` :

* `step()` : Cette méthode va effectuer une itération de l'algorithme et retourner le `Node` qui a été choisi comme minimum pendant cette itération ou bien `null` si l'algorithme est terminé. Nous l'avons extraite de la méthode `doRun()` et définie comme publique afin de pouvoir contrôler l'algorithme au pas à pas, ce qui nous sera utile lors de la résolution du problème ouvert d'échange de colis.
* `doRun()` : Cette méthode se doit d'être implémentée pour les implémentations d'`AbstractAlgorithm`. Elle contient la boucle principale de l'algorithme dans laquelle sera appelé `step()` et renvoie la solution de l'algorithme.  

Lorsque la destination du `ShortestPathData` de l'algorithme est égale à `null`, l'algorithme se terminera uniquement lorsque son tas binaire sera vide. Cela permet d'au besoin connaître les coûts minimaux de tous les sommets atteignables depuis l'origine.

### A*-like Dijkstra

Notre implémentation de l'algorithme A* est basée sur Dijkstra. La seule différence réside dans les `Label` utilisés par l'algorithme. Cet algorithme utilise des `LabelStar`s à la place de simples `Label`. Ceux-ci contiennent, en plus du `Node` et du coût dont ils héritent, une heuristique qui va correspondre à :
* La distance à vol d'oiseau entre le `Node` et la destination lorsque l'on travaille en distance
* Le temps de parcours minimum du vol d'oiseau lorsque l'on travaille en temps.

Cette heuristique ne peut être modifiée après initialisation ce qui permet de préserver l'optimalité de la solution trouvée. En effet, son rôle est d'ordonnancer le choix des `Label` minimaux en privilégiant ceux proches de l'origine **ET** potentiellement proche (en distance ou en temps) de la destination. Pour qu'une heuristique puisse produire une solution optimale, il faut qu'elle représente une borne inférieure du coût du noeud jusqu'à la destination. Or nous nous sommes aperçu que dans certains cas très rares, calculer la distance à vol d'oiseau ne donnait pas toujours une borne inférieure sûrement dû à l'arrondi de la longueur de certains arcs. Même si ces cas sont extrêmement rares et que la solution produite est très proches de la solution optimale, nous avons préféré régler ce problème. Pour ce faire, au lieu de prendre la distance à vol d'oiseau en entier, nous prenons 90% de celle-ci, afin de laisser de la marge et s'assurer qu'on minimise bien le coût réel.   


Cette fois-ci, contrairement à Dijkstra, passer une destination `null` en paramètre de l'algorithme provoquera une `NullPointerException`. En effet, on a besoin d'une destination pour pouvoir calculer l'heuristique. On ne peut donc pas utiliser cet algorithme pour connaître les coûts de tous les sommets atteignables par l'origine en le lançant une seule fois.



## Tests de validité

### Génération des données de test

Les tests se situent au sein de la classe abstraite `ShortestPathAlgorithmTest`. Les classes `DijkstraAlgorithmTest` et `AStarAlgorithmTest` héritent de cette classe et redéfinissent uniquement la méthode `createAlgorithm(...)`. Ainsi, les tests passés par Dijkstra et A* seront exactement les mêmes.

La classe est annotée comme `Parameterized` et chaque `Parameter` est un `ShortestPathData`. La méthode `data()` est en charge de la génération de ces données.

Elle effectue d'abord la génération de tous les couples origine/destination possibles d'un graph très simple à 6 sommets, ce qui fait en tout 6x5=30 données de test :

<insérer image ici>

Ensuite, elle prend une carte ("Maps/bordeaux.mapgr" en l'occurence) et génère 8 couples aléatoires origine/destination. Chacun de ces couples est associé à chacun des `ArcInspector` disponibles via `ArcInspectorFactory` (5 en tout). On a donc 8*5=40 données de test supplémentaires.

### Tests avec oracle

L'oracle en question est Bellman-Ford et est considéré comme exacte, c'est-à-dire qu'il donnera systématiquement une solution optimale si elle existe. Le test passe si les solutions proposées par l'oracle et par l'algorithme sont toutes les deux `INFEASIBLE` ou alors qu'elles sont `FEASIBLE` et que les chemins de leurs solutions sont de même longueur lorsqu'on travaille en distance ou de même temps minimum de trajet lorsqu'on travaille en temps.

### Tests sans oracle

Si le chemin `a -> b` est optimal, alors sa longueur ou temps de trajet minimum est la borne inférieure de tout autre chemin `a -> c -> b` produit par l'algorithme.

On fait calculer la solution `a -> b` par l'algorithme. On choisit ensuite un noeud aléatoire `c` sur la carte. On vérifie que la somme des coûts `a -> c` et `c -> b` produits par l'algorithme soit inférieure au coût de la solution `a -> b`. Dans nos tests, nous répétons ce processus 5 fois pour chaque donnée de test.   

## Tests de performance

### Organisation des tests

### Résultats

## Problème ouvert : Échange de colis

Lexique :
* O1 : origine du premier robot
* D1 : destination du premier robot
* O2 : origine du second robot
* D2 : destination du second robot

Notre méthode va faire s'étendre 4 Dijkstras depuis chacun de ces points. Nous avons donc 4 algorithmes :
* DO1 : le Dijkstra qui s'étend depuis O1
* DD1 : le Dijkstra qui s'étend depuis D1 (s'applique sur la transposée du graphe)
* DO2 : le Dijkstra qui s'étend depuis O2
* DD2 : le Dijkstra qui s'étend depuis D2 (s'applique sur la transposée du graphe)

Pour chaque point du graphe, on compte le nombre de fois qu'il a été marqué par un algorithme. Le point de rencontre est donc le premier point atteint par les 4 Dijkstras, à condition qu'ils s'étendent à la même vitesse. Pour avoir une expansion équivalente sur tous les algorithmes, nous faisons avancer d'une itération le Dijkstra le moins étendu en priorité (via sa méthode `step()`). Ainsi si un Dijkstra évolue dans un milieu dense il effectuera plus d'itérations que les autres mais les 4 auront des étendues comparables. Le point de rencontre (PR) se situera donc à équidistance des 4 points. Retracer les trajets que doivent prendre chacun des robots est trivial :
* On concatène les chemins de O1 à PR (donné par DO1) et de PR à D1 (transposée du chemin de D1 à PR donné par DD1).
* On concatène les chemins de O2 à PR (donné par DO2) et de PR à D2 (transposée du chemin de D2 à PR donné par DD2).

Cette solution a été implémentée sous la forme de l'algorithme `DijkstraExpansionAlgorithm` du package `org.insa.algo.packageswitch`. Nous avons aussi complété les classes `PackageSwitchGraphicObserver` et `PackageSwitchSolution` afin de pouvoir visualiser facilement le déroulement de l'algorithme. Un noeud atteint une fois sera coloré en cyan, deux fois en bleu, trois fois en rose et quatre fois en magenta (seul PR devrait être en magenta). Enfin, quand la solution a été trouvée, on met un marker sur PR et on trace les deux chemins. Notre méthode peut facilement se généraliser à n robots.

## Conclusion
