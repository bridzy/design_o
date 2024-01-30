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
