# BE Graphes INSA Toulouse

## Auteurs

* Sylvain Dupouy
* Léo Picou

## Introduction

Ce bureau d'étude regroupe à la fois la pratique d'un langage orienté objet (Java) ainsi que l'implémentation d'algorithmes relevant de la théorie des graphes. Le projet initial est disponible [ici](https://duckduckgo.com). Les fonctionnalités implémentées sont :
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

### A*-like Dijkstra

Notre implémentation de l'algorithme A* est basée sur Dijkstra. La seule différence réside dans les `Label` utilisés par l'algorithme. Cet algorithme utilise des `LabelStar`s à la place de simples `Label`. Ceux-ci contiennent, en plus du `Node` et du coût dont ils héritent, une heuristique qui va correspondre à :
* La distance à vol d'oiseau entre le `Node` et la destination lorsque l'on travaille en distance
* Le temps de parcours minimum du vol d'oiseau lorsque l'on travaille en temps
Cette heuristique ne peut être modifiée après initialisation ce qui permet de préserver l'optimalité de la solution trouvée. En effet, son rôle est d'ordonnancer le choix des `Label` minimaux en privilégiant ceux proches de l'origine **ET** potentiellement proche (en distance ou en temps) de la destination. Cet ordonnancement peut ne pas être judicieux dans tous les cas, par exemple si un obstacle important se situe entre l'origine et la destination. Néanmoins il s'agit uniquement de l'ordre dans lequel seront choisis les `Label` minimaux à chaque itération, s'il existe une solution, elle sera trouvée.

## Tests de validité

### Génération des données de test

### Tests avec oracle

### Tests sans oracle

## Tests de performance

### Organisation des tests

### Résultats

## Problème ouvert : Échange de colis

## Conclusion
