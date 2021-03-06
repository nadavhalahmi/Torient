package app.shynline.torient.torrent.message.http

import app.shynline.torient.torrent.bencoding.BDict
import app.shynline.torient.torrent.bencoding.BString
import app.shynline.torient.torrent.bencoding.common.BItem
import app.shynline.torient.torrent.bencoding.common.InvalidBencodedException
import app.shynline.torient.torrent.message.BaseMessage
import java.io.IOException
import java.nio.ByteBuffer


class HTTPErrorMessage private constructor(
    data: ByteBuffer,
    private val reason: String
) : BaseHTTPMessage(Type.ERROR, data), BaseMessage.ErrorMessage {


    override fun getReason(): String? {
        return reason
    }

    companion object {
        @Throws(IOException::class, MessageValidationException::class)
        fun parse(data: ByteBuffer): HTTPErrorMessage? {
            data.rewind()
            val dataBA = ByteArray(data.remaining())
            data.get(dataBA)
            val decoded = try {
                BDict(bencoded = dataBA)
            } catch (e: Exception) {
                throw MessageValidationException("Could not decode tracker message!")
            }

            return try {
                HTTPErrorMessage(
                    data,
                    decoded["failure reason"]!!.getBString().toPureString()
                )
            } catch (e: InvalidBencodedException) {
                throw MessageValidationException("Invalid tracker error message!", e)
            }
        }

        @Throws(IOException::class, MessageValidationException::class)
        fun create(
            reason: ErrorMessage.Reason
        ): HTTPErrorMessage? {
            return create(reason.message)
        }

        @Throws(IOException::class, MessageValidationException::class)
        fun create(
            reason: String?
        ): HTTPErrorMessage? {
            val response: LinkedHashMap<BString, BItem<*>> = linkedMapOf()
            response[BString(item = "failure reason")] = BString(item = (reason ?: ""))
            return HTTPErrorMessage(
                ByteBuffer.wrap(BDict(item = response).encode()),
                reason!!
            )
        }
    }
}