package com.salman.moneymanagementapp.model

class Shopping() {
    private var id = 0
    var user_id = 0
    var item_id = 0
    var transaction_id = 0
    var price = 0.0
    var description: String? = null
    var date: String? = null

    constructor(
        id: Int,
        user_id: Int,
        item_id: Int,
        transaction_id: Int,
        price: Double,
        description: String?,
        date: String?
    ) : this() {
        this.id = id
        this.user_id = user_id
        this.item_id = item_id
        this.transaction_id = transaction_id
        this.price = price
        this.description = description
        this.date = date
    }

    fun get_id(): Int {
        return id
    }

    fun set_id(_id: Int) {
        this.id = id
    }

    override fun toString(): String {
        return "Shopping{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", item_id=" + item_id +
                ", transaction_id=" + transaction_id +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                '}'
    }
}
