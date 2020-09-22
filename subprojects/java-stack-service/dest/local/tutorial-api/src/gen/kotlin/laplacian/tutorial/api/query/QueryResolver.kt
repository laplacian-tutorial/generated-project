package laplacian.tutorial.api.query
import laplacian.tutorial.entity.album.AlbumEntity
import laplacian.tutorial.entity.album.AlbumRepository
import laplacian.tutorial.entity.album.AlbumSearchInput
import laplacian.tutorial.entity.comment.CommentEntity
import laplacian.tutorial.entity.comment.CommentRepository
import laplacian.tutorial.entity.comment.CommentSearchInput
import laplacian.tutorial.entity.photo.PhotoEntity
import laplacian.tutorial.entity.photo.PhotoRepository
import laplacian.tutorial.entity.photo.PhotoSearchInput
import laplacian.tutorial.entity.post.PostEntity
import laplacian.tutorial.entity.post.PostRepository
import laplacian.tutorial.entity.post.PostSearchInput
import laplacian.tutorial.entity.task.TaskEntity
import laplacian.tutorial.entity.task.TaskRepository
import laplacian.tutorial.entity.task.TaskSearchInput
import laplacian.tutorial.entity.user.UserEntity
import laplacian.tutorial.entity.user.UserRepository
import laplacian.tutorial.entity.user.UserSearchInput
import laplacian.tutorial.entity.indexed_comment.*

import laplacian.tutorial.api.rpc.rest.gsheets_spreadsheet.*


import org.springframework.stereotype.Component
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import graphql.schema.DataFetchingEnvironment
import java.util.concurrent.CompletableFuture

@Component
class QueryResolver(
    val albumRepository: AlbumRepository,
    val commentRepository: CommentRepository,
    val photoRepository: PhotoRepository,
    val postRepository: PostRepository,
    val taskRepository: TaskRepository,
    val userRepository: UserRepository,
    val indexedCommentSearcher: IndexedCommentSearcher,
    val gsheetsSpreadsheetRestResource: GsheetsSpreadsheetRestResource,
) {
    fun album(context: DataFetchingEnvironment): CompletableFuture<AlbumEntity> =
        context
        .getDataLoader<AlbumEntity, AlbumEntity>(ALBUM_BY_PK)
        .load(AlbumEntity().apply {
            this.id = context.getArgument("id")
        })

    fun albums(context: DataFetchingEnvironment): CompletableFuture<List<AlbumEntity>> =
        albumRepository
        .findAlbums(AlbumSearchInput.from(context.arguments))
        .thenApply{ it.toList() }

    fun numberOfAlbums(context: DataFetchingEnvironment): CompletableFuture<Long> =
        albumRepository
        .countAlbums(AlbumSearchInput.from(context.arguments))

    fun comment(context: DataFetchingEnvironment): CompletableFuture<CommentEntity> =
        context
        .getDataLoader<CommentEntity, CommentEntity>(COMMENT_BY_PK)
        .load(CommentEntity().apply {
            this.postId = context.getArgument("postId")
            this.seqNumber = context.getArgument("seqNumber")
        })

    fun comments(context: DataFetchingEnvironment): CompletableFuture<List<CommentEntity>> =
        commentRepository
        .findComments(CommentSearchInput.from(context.arguments))
        .thenApply{ it.toList() }

    fun numberOfComments(context: DataFetchingEnvironment): CompletableFuture<Long> =
        commentRepository
        .countComments(CommentSearchInput.from(context.arguments))

    fun indexedComments(context: DataFetchingEnvironment): CompletableFuture<List<IndexedCommentDocument>> =
        indexedCommentSearcher
        .findComments(IndexedCommentSearchInput.from(context.arguments))
        .thenApply{ it.toList() }


    fun photo(context: DataFetchingEnvironment): CompletableFuture<PhotoEntity> =
        context
        .getDataLoader<PhotoEntity, PhotoEntity>(PHOTO_BY_PK)
        .load(PhotoEntity().apply {
            this.id = context.getArgument("id")
        })

    fun photos(context: DataFetchingEnvironment): CompletableFuture<List<PhotoEntity>> =
        photoRepository
        .findPhotos(PhotoSearchInput.from(context.arguments))
        .thenApply{ it.toList() }

    fun numberOfPhotos(context: DataFetchingEnvironment): CompletableFuture<Long> =
        photoRepository
        .countPhotos(PhotoSearchInput.from(context.arguments))
    fun post(context: DataFetchingEnvironment): CompletableFuture<PostEntity> =
        context
        .getDataLoader<PostEntity, PostEntity>(POST_BY_PK)
        .load(PostEntity().apply {
            this.id = context.getArgument("id")
        })

    fun posts(context: DataFetchingEnvironment): CompletableFuture<List<PostEntity>> =
        postRepository
        .findPosts(PostSearchInput.from(context.arguments))
        .thenApply{ it.toList() }

    fun numberOfPosts(context: DataFetchingEnvironment): CompletableFuture<Long> =
        postRepository
        .countPosts(PostSearchInput.from(context.arguments))
    fun task(context: DataFetchingEnvironment): CompletableFuture<TaskEntity> =
        context
        .getDataLoader<TaskEntity, TaskEntity>(TASK_BY_PK)
        .load(TaskEntity().apply {
            this.id = context.getArgument("id")
        })

    fun tasks(context: DataFetchingEnvironment): CompletableFuture<List<TaskEntity>> =
        taskRepository
        .findTasks(TaskSearchInput.from(context.arguments))
        .thenApply{ it.toList() }

    fun numberOfTasks(context: DataFetchingEnvironment): CompletableFuture<Long> =
        taskRepository
        .countTasks(TaskSearchInput.from(context.arguments))
    fun user(context: DataFetchingEnvironment): CompletableFuture<UserEntity> =
        context
        .getDataLoader<UserEntity, UserEntity>(USER_BY_PK)
        .load(UserEntity().apply {
            this.id = context.getArgument("id")
        })

    fun users(context: DataFetchingEnvironment): CompletableFuture<List<UserEntity>> =
        userRepository
        .findUsers(UserSearchInput.from(context.arguments))
        .thenApply{ it.toList() }

    fun numberOfUsers(context: DataFetchingEnvironment): CompletableFuture<Long> =
        userRepository
        .countUsers(UserSearchInput.from(context.arguments))

    fun getSpreadsheetById(context: DataFetchingEnvironment): CompletableFuture<GetSpreadsheetByIdResponse> =
        gsheetsSpreadsheetRestResource
        .getSpreadsheetById(GetSpreadsheetByIdRequest(
            spreadsheetId = context.getArgument("spreadsheetId"),
        ))

    fun registerFetcher(wiring: RuntimeWiring.Builder) = wiring.type(
        TypeRuntimeWiring.newTypeWiring("Query")
        .dataFetcher("albums") { env -> albums(env) }
        .dataFetcher("numberOfAlbums") { env -> numberOfAlbums(env) }
        .dataFetcher("album") { env -> album(env)}
        .dataFetcher("comments") { env -> comments(env) }
        .dataFetcher("numberOfComments") { env -> numberOfComments(env) }
        .dataFetcher("comment") { env -> comment(env)}
        .dataFetcher("indexedComments") { env -> indexedComments(env)}
        .dataFetcher("photos") { env -> photos(env) }
        .dataFetcher("numberOfPhotos") { env -> numberOfPhotos(env) }
        .dataFetcher("photo") { env -> photo(env)}
        .dataFetcher("posts") { env -> posts(env) }
        .dataFetcher("numberOfPosts") { env -> numberOfPosts(env) }
        .dataFetcher("post") { env -> post(env)}
        .dataFetcher("tasks") { env -> tasks(env) }
        .dataFetcher("numberOfTasks") { env -> numberOfTasks(env) }
        .dataFetcher("task") { env -> task(env)}
        .dataFetcher("users") { env -> users(env) }
        .dataFetcher("numberOfUsers") { env -> numberOfUsers(env) }
        .dataFetcher("user") { env -> user(env)}
        .dataFetcher("gsheetsSpreadsheet") { env -> getSpreadsheetById(env)}
    )

    companion object {
        const val ALBUM_BY_PK = "album_byPK"
        const val COMMENT_BY_PK = "comment_byPK"
        const val PHOTO_BY_PK = "photo_byPK"
        const val POST_BY_PK = "post_byPK"
        const val TASK_BY_PK = "task_byPK"
        const val USER_BY_PK = "user_byPK"
    }
}