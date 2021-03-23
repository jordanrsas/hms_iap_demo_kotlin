package com.dtse.demo.iap.huawei.utils

import android.util.Base64
import android.util.Log
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

object CipherUtil {
    private const val TAG = "CipherUtil"
    private const val SIGN_ALGORITHMS = "SHA256WithRSA"
    const val PUBLIC_KEY =
        "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAnm4SsjTlLBmp0zJh0qe4faPwQmOtJ/HO43jQa4hEjudEAIMFvO0GNNsRJ2TQVsXzD+BlB25rgevNtuzHzfH3Id/s1Jffu72+qrI6xBN3o62uMNnpE6TXspxd0++lfafS6SxFza9dp7wcHoudlzIB0W4C8bKK5s8yZnQc9uIyI+k50WZun00QblJrhjdKE5G89k+1Je3bzYI+M38t1qTI0+TSYTA8NOfB7eDr+nNClhB6vCEcDaVQNQLe+VZEkg0pFQndGXt2abFSukveqTXG7a1yUVfirDvSwvIG4jI6x43iEfIXewyE725TYdZkTvOoDftBqSQrcvK7oNp0wQi1KerJUbuHXVThwm+0NWANf0OwhXs+rRENB9CyVoK1XFmkapXp4L6+TBxl/YvK8oMkDuYO6wMLkDjbDkU8aZDmbHMZ8sH9IdeAyPWr8aBZUuJgLP6YjmiUOKoTMDwWJ8j5O2mLnLCHtpp/t78bB/yIeT3bjFrMQ1vt6yhs1W7P6odxAgMBAAE="


    /**
     * the method to check the signature for the data returned from the interface
     * @param content Unsigned data
     * @param sign the signature for content
     * @param publicKey the public of the application
     * @return boolean
     */
    public fun doCheck(content: String, sign: String, publicKey: String): Boolean {
        if (publicKey.isEmpty()) {
            Log.e(TAG, "publicKey is null")
            return false
        }

        try {
            val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
            val encodedKey = Base64.decode(publicKey, Base64.DEFAULT)
            val pubKey = keyFactory.generatePublic(X509EncodedKeySpec(encodedKey))

            val signature = Signature.getInstance(SIGN_ALGORITHMS)
            signature.initVerify(pubKey)
            signature.update(content.toByteArray(Charset.forName("utf-8")))
            return signature.verify(Base64.decode(sign, Base64.DEFAULT))
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "doCheck NoSuchAlgorithmException $e")
        } catch (e: InvalidKeySpecException) {
            Log.e(TAG, "doCheck InvalidKeySpecException $e")
        } catch (e: InvalidKeyException) {
            Log.e(TAG, "doCheck InvalidKeyException $e")
        } catch (e: SignatureException) {
            Log.e(TAG, "doCheck SignatureException $e")
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "doCheck UnsupportedEncodingException $e")
        }
        return false
    }
}