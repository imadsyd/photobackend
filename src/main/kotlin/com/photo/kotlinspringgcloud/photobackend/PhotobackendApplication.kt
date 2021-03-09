
/**https://youtu.be/VG7rw4uPn3A*/

package com.photo.kotlinspringgcloud.photobackend

import com.google.cloud.spring.data.datastore.core.mapping.Entity
import com.google.cloud.spring.data.datastore.repository.DatastoreRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.core.io.WritableResource
import org.springframework.data.annotation.Id
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@SpringBootApplication
class PhotobackendApplication

fun main(args: Array<String>) {
    runApplication<PhotobackendApplication>(*args)
}

@Entity
data class Photo2(
        @Id var id: String? = null,
        var uri: String? = null,
        var label: String? = null
)

@RepositoryRestResource
interface PhotoRepository: DatastoreRepository<Photo2, String>

@RestController
class UploadController(
        private val ctx : ApplicationContext,
        private val photoRepository: PhotoRepository
) {

    private val bucket = "gs://photobackend2.appspot.com/images";


    @PostMapping("/upload")
    fun upload(@RequestParam("file") file: MultipartFile): Photo2 {

        val id = UUID.randomUUID().toString()
        val uri = "$bucket/$id"

        val gcs = ctx.getResource(uri) as WritableResource

        file.inputStream.use{ input ->
            gcs.outputStream.use { output ->
                input.copyTo(output)

            }
        }

        return photoRepository.save(Photo2(
                id = id,
                uri = "/image/$id"
        )
        )

    }

    @GetMapping("/image/{id}")
    fun get(@PathVariable id: String): ResponseEntity<Resource> {
        val resource = ctx.getResource("$bucket/$id")
        return if (resource.exists()){
            ResponseEntity.ok(resource)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }


    }
}
    @RestController
    class HelloController(
            private val photoRepository: PhotoRepository

    ) {
        @GetMapping("/")
        fun hello() = "hello!"

        @PostMapping("/photo2")
        fun create(@RequestBody photo: Photo2) {
            photoRepository.save(photo)

        }
    }


