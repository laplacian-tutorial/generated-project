# **Task**
**namespace:** laplacian.tutorial

task



---

## Properties

### id: `Int`
The id of this task.
- **Attributes:** *PK*

### user_id: `Int`
The user_id of this task.

### title: `String`
The title of this task.
- **Default Value:**
  ```kotlin
  ""
  ```

### completed: `Boolean`
Defines this task is completed or not.
- **Default Value:**
  ```kotlin
  false
  ```

## Relationships

### assignee: `User?`
assignee
- **Cardinality:** `0..1`