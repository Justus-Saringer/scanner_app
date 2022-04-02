package de.htw_berlin.qrdenker.firebase.model

import de.htw_berlin.qrdenker.RecordConstructorRecyclerViewItem

class DocumentModel {

    companion object{
        const val COLLECTION_PATH = "COLLECTION PATH"

        const val ID_KEY = "ID"

        const val TITLE_KEY = "TITLE"
        //const val IMAGE_FOLDER_PATH_KEY = "IMAGE_FOLDER_PATH"
        const val BODY_KEY = "BODY"
    }
    var collectionPath = ""
    var id = ""
    var title = ""
    //var imageFolderPath = ""
    var body = mutableListOf<RecordConstructorRecyclerViewItem>()

    fun toDictionary(): MutableMap<String, Any>{
        val map = mutableMapOf<String, Any>()

        map[COLLECTION_PATH] = collectionPath
        map[ID_KEY] = id
        map[TITLE_KEY] = title
        //map[IMAGE_FOLDER_PATH_KEY] = imageFolderPath
        var bodyList: MutableList<Map<String,Any?>> = mutableListOf()
        for (item in body){
            when(item){
                is RecordConstructorRecyclerViewItem.Image ->{
                    val tempMap = mapOf<String, String>(
                        "imageName" to item.imageName,
                        "imageUrl" to item.imageUrl
                    )

                    bodyList.add(tempMap)
                }
                is RecordConstructorRecyclerViewItem.Heading ->{
                    val tempMap = mapOf<String, String>(
                    "heading" to item.heading
                    )
                    bodyList.add(tempMap)
                }
                is RecordConstructorRecyclerViewItem.Text ->{
                    val tempMap = mapOf<String, String>(
                        "text" to item.text
                    )
                    bodyList.add(tempMap)
                }
            }

        }

        map[BODY_KEY] = bodyList
        return map
    }

    fun parsing(map: MutableMap<String,Any>){
        id = map[ID_KEY] as String
        title = map[TITLE_KEY] as String
        //imageFolderPath = map[IMAGE_FOLDER_PATH_KEY] as String
        val bodyMap = map[BODY_KEY] as MutableList<HashMap<String, String>>
        for (itemMap in bodyMap){
            for(key in itemMap.keys){
                if (key == "heading" && !itemMap[key].isNullOrBlank()) body.add(RecordConstructorRecyclerViewItem.Heading(itemMap[key]!!))
                else if (key == "imageName" && !itemMap[key].isNullOrBlank()) body.add(RecordConstructorRecyclerViewItem.Image(null,itemMap[key]!!,itemMap["imageUrl"]!!))
                else if (key == "text" && !itemMap[key].isNullOrBlank()) body.add(RecordConstructorRecyclerViewItem.Text(itemMap[key]!!))

            }
        }
    }

}