package com.salman.moneymanagementapp.model

class Loan {
    private var id = 0
    var name: String? = null
    var init_amount = 0.0
    var remained_amount = 0.0
    var monthly_roi = 0.0
    var monthly_payment = 0.0
    var user_id = 0
    var transaction_id = 0
    var init_date: String? = null
    var finish_date: String? = null

    constructor(
        id: Int,
        name: String?,
        init_amount: Double,
        remained_amount: Double,
        monthly_roi: Double,
        monthly_payment: Double,
        user_id: Int,
        transaction_id: Int,
        init_date: String?,
        finish_date: String?
    ) {
        this.id = id
        this.name = name
        this.init_amount = init_amount
        this.remained_amount = remained_amount
        this.monthly_roi = monthly_roi
        this.monthly_payment = monthly_payment
        this.user_id = user_id
        this.transaction_id = transaction_id
        this.init_date = init_date
        this.finish_date = finish_date
    }

    constructor() {}

    fun get_id(): Int {
        return id
    }

    fun set_id(id: Int) {
        this.id = id
    }

    override fun toString(): String {
        return "Loan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", init_amount=" + init_amount +
                ", remained_amount=" + remained_amount +
                ", monthly_roi=" + monthly_roi +
                ", monthly_payment=" + monthly_payment +
                ", user_id=" + user_id +
                ", transaction_id=" + transaction_id +
                ", init_date='" + init_date + '\'' +
                ", finish_date='" + finish_date + '\'' +
                '}'
    }
}
