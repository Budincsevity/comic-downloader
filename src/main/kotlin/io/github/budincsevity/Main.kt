package io.github.budincsevity

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object Services {
    private val retrofit = Retrofit.Builder()
            .baseUrl("http://readcomicbooksonline.net/reader/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    internal val comicDownloader = retrofit.create(ComicDownloader::class.java)
}

fun main(args: Array<String>) {

    if (args.isEmpty() || args.size > 1) {
        throw IllegalArgumentException("Only one argument is allowed which is the URL to the Comic. e.g.: http://readcomicbooksonline.net/reader/Batman_2016/Batman_2016_Issue_032")
    }

    val comicUrl = args[0]
    val comicName = comicUrl.split("/").last()
    downloadComicImages(comicUrl, comicName, 1)
}

private fun downloadComicImages(comicUrl: String, comicName: String, pageIndex: Int) {

    val document = Jsoup.connect("$comicUrl/$pageIndex").get()
    val elementsByClass = document.getElementsByClass("picture")
    if (elementsByClass.size == 0) {
        return
    }

    val imageSource = elementsByClass.first().attr("src")
    if (imageSource.isBlank()) {
        return
    }

    val downloadComic = Services.comicDownloader.downloadComic(imageSource)
    val response = downloadComic.execute()
    val responseBody = response.body()
    if (!response.isSuccessful || responseBody == null) {
        return
    }

    storeImageToTheFilesystem(comicName, pageIndex, responseBody)
    downloadComicImages(comicUrl, comicName, pageIndex + 1)
}

private fun storeImageToTheFilesystem(comicName: String, pageIndex: Int, responseBody: ResponseBody) {
    val directoryToStore = Paths.get(comicName)
    if (Files.notExists(directoryToStore)) {
        Files.createDirectory(directoryToStore)
    }

    File(comicName, "$comicName-$pageIndex.png").writeBytes(responseBody.bytes())
}
