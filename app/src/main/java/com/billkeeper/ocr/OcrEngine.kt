package com.billkeeper.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * OCR引擎封装 - 基于Google ML Kit中文文字识别
 */
object OcrEngine {

    private val recognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    /**
     * 识别Bitmap中的文字
     * @return 识别出的完整文本
     */
    suspend fun recognizeText(bitmap: Bitmap): String = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                cont.resume(visionText.text)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }

    /**
     * 识别Bitmap中的文字，返回结构化结果（包含位置信息）
     * @return OcrResult 包含原始文本和行文本列表
     */
    suspend fun recognizeStructured(bitmap: Bitmap): OcrResult = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val lines = visionText.textBlocks.flatMap { block ->
                    block.lines.map { line ->
                        OcrLine(
                            text = line.text,
                            boundingBox = line.boundingBox,
                            confidence = line.confidence
                        )
                    }
                }
                cont.resume(OcrResult(fullText = visionText.text, lines = lines))
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }
}

/**
 * OCR识别结果
 */
data class OcrResult(
    val fullText: String,
    val lines: List<OcrLine>
)

/**
 * OCR识别行
 */
data class OcrLine(
    val text: String,
    val boundingBox: android.graphics.Rect?,
    val confidence: Float
)
