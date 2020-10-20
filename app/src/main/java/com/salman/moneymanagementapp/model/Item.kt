package com.salman.moneymanagementapp.model

class Item() {
    private var id = 0
    var name: String? = null
    var image_url: String? = null
    var description: String? = null

    constructor(
        id: Int,
        name: String?,
        image_url: String?,
        description: String?
    ) : this() {
        this.id = id
        this.name = name
        this.image_url = image_url
        this.description = description
    }


    fun get_id(): Int {
        return id
    }

    fun set_id(id: Int) {
        this.id = id
    }

    override fun toString(): String {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", image_url='" + image_url + '\'' +
                ", description='" + description + '\'' +
                '}'
    }
}
