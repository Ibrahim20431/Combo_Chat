package com.chats.combochat.model

class Message {
    var messageId: String? = null
    var messageText: String? = null
    var senderId: String? = null
    var imageUrl: String? = null
    var timeStamp: Long? = 0

    constructor(){}

    constructor(
        messageText: String?,
        senderId: String?,
        timeStamp: Long?
    ){
        this.messageText = messageText
        this.senderId = senderId
        this.timeStamp = timeStamp
    }
}