# **Photo**
**namespace:** laplacian.tutorial

photo



---

## Properties

### id: `Int`
The id of this photo.
- **Attributes:** *PK*

### album_id: `Int`
The album_id of this photo.

### title: `String`
The title of this photo.
- **Default Value:**
  ```kotlin
  ""
  ```

### url: `String`
The url of this photo.

### thumbnailUrl: `String`
The thumbnailUrl of this photo.

### date_taken: `String`
The date_taken of this photo.

## Relationships

### album: `Album`
album
- **Cardinality:** `1`