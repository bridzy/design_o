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

---

Ce résumé met en lumière la méthodologie suivie pour refactoriser le code Java initial, en se concentrant sur l'amélioration de la structure et la conformité aux principes SOLID, et illustre l'impact positif de ces principes sur la conception logicielle.

----------------------------------------------------------------------------------------------------------------------------------------------------------
TP02 : 
# Modifications du Code pour Gérer les Todos Accomplis

## Introduction
Les modifications suivantes ont été apportées au code initial pour introduire la gestion des tâches (todos) marquées comme accomplies. Cela inclut l'ajout d'un paramètre `--done` lors de l'ajout d'une tâche et la possibilité de lister uniquement les tâches accomplies.

## Modifications Apportées

### Ajout du Paramètre `--done`
- Le parseur de ligne de commande `CommandLineHandler` a été mis à jour pour reconnaître le paramètre `--done`.
- Une nouvelle propriété `isDone` a été ajoutée à la classe `Command` pour stocker l'état de la tâche.

### Gestion des Todos Accomplis
- L'interface `FileFormatHandler` a été mise à jour pour inclure deux nouvelles méthodes : `processInsertCommand` et `processListCommand`, qui prennent en compte l'état accompli des tâches.
- La classe `JsonFormatHandler` a été implémentée pour gérer correctement l'ajout et le listage des tâches, en prenant en compte leur état (accompli ou non).

### Implémentation pour le Format JSON
- Les tâches sont maintenant stockées dans un format JSON qui inclut l'état de la tâche (`done` : true ou false).
- La méthode `processInsertCommand` ajoute une nouvelle tâche avec l'indicateur `done` approprié.
- La méthode `processListCommand` a été mise à jour pour filtrer et afficher les tâches en fonction de leur état accompli, avec un préfixe "Done: " pour les tâches accomplies.

### Classe `TodoProcessor`
- La logique de traitement des commandes a été adaptée pour utiliser le gestionnaire de format approprié (`JsonFormatHandler`) et pour traiter correctement les nouvelles fonctionnalités.

## Conclusion
Ces modifications permettent à l'application de gérer de manière flexible les tâches en marquant certaines d'entre elles comme accomplies et en offrant la possibilité de filtrer les tâches basées sur leur état lors du listage.
