# **Album**
**namespace:** laplacian.tutorial

album



---

## Properties

### id: `Int`
The id of this album.
- **Attributes:** *PK*

### user_id: `Int`
The user_id of this album.

### title: `String`
The title of this album.
- **Default Value:**
  ```kotlin
  ""
  ```

## Relationships

### owner: `User`
owner
- **Cardinality:** `1`

### photos: `List<Photo>`
photos
- **Cardinality:** `*`