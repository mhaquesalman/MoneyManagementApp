package com.salman.moneymanagementapp.model

class Transaction() {
    private var id = 0
    var amount = 0.0
    var date: String? = null
    var type: String? = null
    var user_id = 0
    var recipient: String? = null
    var description: String? = null

    constructor(
        id: Int,
        amount: Double,
        date: String?,
        type: String?,
        user_id: Int,
        recipient: String?,
        description: String?
    ) : this() {
        this.id = id
        this.amount = amount
        this.date = date
        this.type = type
        this.user_id = user_id
        this.recipient = recipient
        this.description = description
    }

    fun get_id(): Int {
        return id
    }

    fun set_id(id: Int) {
        this.id = id
    }

    override fun toString(): String {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", user_id=" + user_id +
                ", recipient='" + recipient + '\'' +
                ", description='" + description + '\'' +
                '}'
    }
}
