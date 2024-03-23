# L3 design pattern report

- **Firstname**: Ouail
- **Lastname**: LEKHCHINE


## Refactorisation du Code Java avec les Principes SOLID

### Application des Principes SOLID

#### Single Responsibility Principle (SRP)

- **Séparation des Responsabilités** : Le code a été divisé en classes distinctes avec des responsabilités uniques, telles que la manipulation des fichiers (`CsvFileManager` et `JsonFileManager`) et le traitement des commandes d'insertion et de listage.

#### Open/Closed Principle (OCP)

- **Extensibilité** : Une interface `TodoFileManager` a été créée pour permettre l'extension à d'autres formats de fichiers sans modifier le code existant.

#### Liskov Substitution Principle (LSP)

- Les classes implémentant `TodoFileManager` peuvent être substituées sans affecter le comportement attendu, permettant une flexibilité dans le choix du format de fichier.

#### Interface Segregation Principle (ISP)

- Les interfaces ont été conçues pour être spécifiques aux besoins des clients qui les utilisent, évitant ainsi les dépendances inutiles.

#### Dependency Inversion Principle (DIP)

- Le code de haut niveau ne dépend pas des détails de mise en œuvre des classes de bas niveau mais des abstractions, facilitant ainsi la gestion des dépendances.

### Architecture du Code

L'architecture refactorisée implémente `TodoFileManager` pour gérer les opérations de fichier, avec deux implémentations concrètes pour les formats CSV et JSON. Cela démontre l'application des principes SOLID pour améliorer la structure du code.

```java
interface TodoFileManager {
    void insert(String todo, Path filePath) throws IOException;
    void list(Path filePath) throws IOException;
}
```

#### Implémentations Spécifiques

- **CsvFileManager** : Gère les fichiers CSV.
- **JsonFileManager** : Gère les fichiers JSON.

### Main Application (`App.java`)

La classe principale `App` utilise `TodoFileManager` pour déléguer les opérations d'insertion et de listage à l'implémentation appropriée en fonction du type de fichier spécifié.

### Conclusion

Cette refactorisation a pour but de rendre l'application plus facile à maintenir et à étendre, notamment en facilitant l'ajout de nouveaux formats de fichiers ou de nouvelles commandes sans impacter le code existant. L'application est désormais mieux structurée, avec des composants clairement définis suivant les principes SOLID.


----------------------------------------------------------------------------------------------------------------------------------------------------------
TP02 : 
## Modifications Apportées

### Interface `TodoFileManager`

- **Ajout de Paramètres** : Modification de la signature des méthodes pour inclure la gestion de l'état des tâches (faites ou non) et pour permettre de filtrer les tâches lors de l'affichage.

  ```java
  void insert(String todo, boolean isDone, Path filePath) throws IOException;
  void list(Path filePath, boolean onlyDone) throws IOException;
  ```

### Classe `JsonFileManager`

- **Gestion de l'État des Tâches** : Adaptation pour créer et traiter des objets JSON incluant un champ `"done"` indiquant si une tâche est faite.

- **Vérification des Nullités** : Ajout de vérifications pour éviter les `NullPointerException` lors de la lecture des champs `"task"` et `"done"` des objets JSON.

### Classe `CsvFileManager`

- **Adaptation pour l'État des Tâches** : Modification pour inclure l'état des tâches (faites ou non) dans le fichier CSV et pour filtrer lors de l'affichage.

### Classe `App`

- **Gestion des Arguments CLI** : Ajout de l'option `--done` pour marquer les tâches comme faites lors de l'ajout et pour filtrer les tâches faites lors de l'affichage.

## Correction des Erreurs

- **NullPointerException** : Correction des erreurs causées par des tentatives d'accès à des champs non existants dans les objets JSON, en ajoutant des vérifications de nullité avant d'accéder aux champs `"task"` et `"done"`.

## Code Final Adapté

Les modifications ont été intégrées à travers les différentes classes pour prendre en charge les nouvelles fonctionnalités demandées et pour corriger les erreurs identifiées. Voici les points clés de l'implémentation :

- **Insertion et Liste des Tâches** : Les tâches peuvent désormais être ajoutées avec un état (fait ou non fait), et l'affichage des tâches peut être filtré pour montrer uniquement celles qui sont marquées comme faites.

- **Robustesse** : Le code a été rendu plus robuste par la gestion appropriée des cas où les données attendues pourraient être manquantes ou mal formées.
---------------------------------------------------------------------
TP 03 : 

# Gestion des Tâches TODO

## Introduction
Ce document présente un résumé des choix de conception et des motifs de conception utilisés dans le cadre du TP sur la gestion des tâches TODO.

## Choix de Design Pattern
Les principaux motifs de conception utilisés dans cette application sont **le pattern Strategy** et **le pattern Command** pour structurer le code de manière flexible et maintenable.

### Pattern Strategy
Le pattern Strategy est utilisé pour définir une famille d'algorithmes, encapsuler chacun d'eux et les rendre interchangeables. Dans notre cas, `TodoFileManager` agit comme la stratégie de base pour les opérations sur les fichiers TODO, avec deux stratégies concrètes : `JsonFileManager` et `CsvFileManager`. Cela permet d'ajouter facilement de nouveaux formats de fichier sans modifier le code client.

### Pattern Command
Le pattern Command est employé pour encapsuler une demande en tant qu'objet, permettant ainsi de paramétrer les clients avec des requêtes, des files d'attente ou des opérations. Bien que non explicitement défini dans le code, ce motif est impliqué par la structure des opérations `insert`, `list`, et `migrate` qui peuvent être considérées comme des commandes.

## Impact sur la Base de Code
L'utilisation de ces motifs de conception rend le code plus modulaire, facile à étendre et à maintenir. Par exemple, l'ajout d'un nouveau format de fichier ne nécessite que la création d'une nouvelle classe implémentant `TodoFileManager`. De même, la modification ou l'ajout de nouvelles commandes (opérations) est simplifiée grâce à cette structure flexible.



##Final


# Rapport Final

Ce rapport présente les procédures et les étapes nécessaires pour intégrer de nouvelles fonctionnalités à notre projet, telles que l'ajout de nouvelles commandes, de sources de données basées sur des fichiers ou non, l'ajout de nouveaux attributs à une tâche (Todo), et l'intégration de nouvelles interfaces au projet. Chaque section décrit en détail les étapes à suivre pour chaque ajout, offrant ainsi un guide clair pour les nouveaux contributeurs du projet.

## Ajout d'une Nouvelle Commande

Pour ajouter une nouvelle commande, suivez ces étapes :

1. Dans le fichier `App.java`, créez une nouvelle méthode pour la nouvelle commande en définissant sa signature et sa fonctionnalité.
2. Implémentez la logique de la commande dans la nouvelle méthode, en gérant son exécution, ses options et ses arguments.
3. Assurez-vous de gérer correctement les cas d'erreur et de fournir des informations pertinentes en cas d'échec.

## Ajout d'une Source de Données Basée sur un Fichier

Pour ajouter une nouvelle source de données basée sur un fichier, suivez ces étapes :

1. Créez une nouvelle classe implémentant l'interface `TodoFileManager`.
2. Implémentez les méthodes requises (`insert` et `list`) en fonction du nouveau format de fichier.
3. Assurez-vous de gérer correctement les opérations basées sur le fichier, telles que l'écriture et la lecture.

## Ajout d'une Source de Données Non Basée sur un Fichier

Pour ajouter une nouvelle source de données non basée sur un fichier, suivez ces étapes :

1. Créez une nouvelle classe implémentant l'interface `TodoFileManager`.
2. Implémentez les méthodes requises (`insert` et `list`) en fonction du type de source de données.
3. Assurez-vous de gérer correctement les opérations non basées sur le fichier, en fonction des besoins du projet.

## Ajout d'un Nouvel Attribut à une Tâche (Todo)

Pour ajouter un nouvel attribut à une tâche, suivez ces étapes :

1. Définissez le nouvel attribut et son type dans la classe `Todo`.
2. Mettez à jour la classe `Todo` pour inclure le nouvel attribut, en modifiant les getters et les setters au besoin.
3. Assurez-vous que les modifications sont cohérentes avec le reste du code et qu'elles n'introduisent pas de régressions.

## Ajout d'une Nouvelle Interface au Projet

Pour ajouter une nouvelle interface au projet, suivez ces étapes :

1. Créez une nouvelle interface définissant les méthodes nécessaires.
2. Implémentez cette interface dans les classes pertinentes du projet.
3. Assurez-vous que toutes les méthodes de l'interface sont correctement implémentées dans les classes concernées.



## Implémentation de la Commande / web

### Difficultés Rencontrées
Malheureusement, malgré les efforts déployés, l'implémentation de la commande `/ web` n'a pas été réalisée avec succès dans le cadre de ce projet.

### Raisons des Difficultés
Plusieurs facteurs ont contribué aux difficultés rencontrées lors de l'implémentation de la commande `/ web`. Parmi les principaux :
- La complexité de la mise en place d'un serveur web avec les fonctionnalités de REST API.
- La nécessité d'interagir avec des requêtes HTTP et de comprendre le fonctionnement des protocoles de communication.
- Le manque de temps et de ressources pour se familiariser avec les concepts avancés nécessaires à cette implémentation.

### Perspectives d'Amélioration
Malgré l'échec de cette tentative, des pistes d'amélioration peuvent être explorées à l'avenir pour réussir l'implémentation de la commande `/ web`. Parmi les possibilités :
- Consulter des ressources supplémentaires sur la mise en place de serveurs web en Java.
- Rechercher des tutoriels et des exemples de projets similaires pour comprendre les meilleures pratiques.
- Collaborer avec des pairs ou des experts ayant une expertise dans ce domaine pour obtenir de l'aide et des conseils.

### Conclusion
Bien que l'implémentation de la commande `/ web` n'ait pas été achevée, cette expérience a permis d'identifier les défis et les lacunes à combler pour réussir des projets similaires à l'avenir. Il est important de tirer des leçons de cette expérience et de continuer à progresser dans l'apprentissage et la maîtrise des concepts liés au développement web en Java.




## Diagramme de Classe
Ci-dessous, le diagramme de classe UML illustre la structure du code :

```mermaid
classDiagram
    class TodoFileManager {
      <<interface>>
      +insert(String todo, boolean isDone, Path filePath) void
      +list(Path filePath, boolean onlyDone) void
      +readAll(Path filePath) List~String[]~
    }

    class JsonFileManager {
      -mapper ObjectMapper
      +insert(String todo, boolean isDone, Path filePath) void
      +list(Path filePath, boolean onlyDone) void
      +readAll(Path filePath) List~String[]~
    }

    class CsvFileManager {
      +insert(String todo, boolean isDone, Path filePath) void
      +list(Path filePath, boolean onlyDone) void
      +readAll(Path filePath) List~String[]~
    }

    class App {
      +main(String[] args) void
      +exec(String[] args) int
      -migrate(Path sourcePath, Path outputPath) void
      -getFileManager(Path filePath) TodoFileManager
    }

    TodoFileManager <|.. JsonFileManager : implements
    TodoFileManager <|.. CsvFileManager : implements
    App --> TodoFileManager : uses

