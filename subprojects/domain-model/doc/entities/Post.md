# **Post**
**namespace:** laplacian.tutorial

post



---

## Properties

### id: `Int`
The id of this post.
- **Attributes:** *PK*

### user_id: `Int`
The user_id of this post.

### title: `String`
The title of this post.

### body: `String`
The body of this post.

## Relationships

### posted_by: `User?`
posted_by
- **Cardinality:** `0..1`

### comments: `List<Comment>`
comments
- **Cardinality:** `*`