# L3 design pattern report

- **Firstname**: Ouail
- **Lastname**: LEKHCHINE


## Modifications Appliquées

### Single Responsibility Principle (SRP)
- **`CommandLineHandler`** pour analyser les arguments CLI.
- **`TodoProcessor`** pour gérer la logique de traitement des tâches.

### Open-Closed Principle (OCP)
- **`FileFormatHandler`** comme interface pour les opérations de formatage de fichiers.
- **`JsonFormatHandler`** et **`CsvFormatHandler`** implémentent `FileFormatHandler`.

### Liskov Substitution Principle (LSP)
- **`JsonFormatHandler`** et **`CsvFormatHandler`** sont utilisables de manière interchangeable là où `FileFormatHandler` est requis.

### Interface Segregation Principle (ISP)
- **`FileFormatHandler`** fournit une interface ciblée pour la gestion des formats de fichiers.

### Dependency Inversion Principle (DIP)
- **`TodoProcessor`** dépend des abstractions (`FileFormatHandler`), et non des détails de mise en œuvre (`JsonFormatHandler`, `CsvFormatHandler`).

## Conclusion
Les modifications apportées au code alignent l'application avec les principes SOLID, favorisant une meilleure extensibilité, maintenabilité et flexibilité.

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
