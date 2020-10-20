package com.salman.moneymanagementapp.model

class Investment {
    private var id = 0
    var user_id = 0
    var transaction_id = 0
    var name: String? = null
    var amount = 0.0
    var init_date: String? = null
    var finish_date: String? = null
    var monthly_roi = 0.0

    constructor(
        id: Int,
        user_id: Int,
        transaction_id: Int,
        name: String?,
        amount: Double,
        init_date: String?,
        finish_date: String?,
        monthly_roi: Double
    ) {
        this.id = id
        this.user_id = user_id
        this.transaction_id = transaction_id
        this.name = name
        this.amount = amount
        this.init_date = init_date
        this.finish_date = finish_date
        this.monthly_roi = monthly_roi
    }

    constructor() {}

    fun get_id(): Int {
        return id
    }

    fun set_id(id: Int) {
        this.id = id
    }

    override fun toString(): String {
        return "Investment{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", transaction_id=" + transaction_id +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", init_date='" + init_date + '\'' +
                ", finish_date='" + finish_date + '\'' +
                ", monthly_roi=" + monthly_roi +
                '}'
    }
}
