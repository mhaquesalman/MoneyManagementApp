package com.salman.moneymanagementapp.model

// primary constructor
class User() {
    private var id = 0
    var email: String? = null
    var password: String? = null
    var first_name: String? = null
    var last_name: String? = null
    var address: String? = null
    var image_url: String? = null
    var remained_amount = 0.0

    // secondary constructor
    constructor(
        id: Int,
        email: String?,
        password: String?,
        first_name: String?,
        last_name: String?,
        address: String?,
        image_url: String?,
        remained_amount: Double
    ) : this() {
        this.id = id
        this.email = email
        this.password = password
        this.first_name = first_name
        this.last_name = last_name
        this.address = address
        this.image_url = image_url
        this.remained_amount = remained_amount
    }

    fun get_id(): Int {
        return id
    }

    fun set_id(_id: Int) {
        this.id = id
    }

    override fun toString(): String {
        return "User{" +
                "  id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", address='" + address + '\'' +
                ", image_url='" + image_url + '\'' +
                ", remained_amount=" + remained_amount +
                '}'
    }
}
