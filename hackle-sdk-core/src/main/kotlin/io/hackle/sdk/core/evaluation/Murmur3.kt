package io.hackle.sdk.core.evaluation

/**
 * @author Yong
 */
@Suppress("FunctionName")
internal object Murmur3 {

    fun murmurhash3_x86_32(data: CharSequence, seed: Int): Int {
        return murmurhash3_x86_32(data, 0, data.length, seed)
    }

    fun murmurhash3_x86_32(data: CharSequence, offset: Int, len: Int, seed: Int): Int {
        val c1 = -0x3361d2af
        val c2 = 0x1b873593

        var h1 = seed

        var pos = offset
        val end = offset + len
        var k1 = 0
        var k2: Int
        var shift = 0
        var bits: Int
        var nBytes = 0 // length in UTF8 bytes

        while (pos < end) {
            val code = data[pos++].toInt()
            if (code < 0x80) {
                k2 = code
                bits = 8
            } else if (code < 0x800) {
                k2 = (0xC0 or (code shr 6)
                        or (0x80 or (code and 0x3F) shl 8))
                bits = 16
            } else if (code < 0xD800 || code > 0xDFFF || pos >= end) {
                // we check for pos>=end to encode an unpaired surrogate as 3 bytes.
                k2 = (0xE0 or (code shr 12)
                        or (0x80 or (code shr 6 and 0x3F) shl 8)
                        or (0x80 or (code and 0x3F) shl 16))
                bits = 24
            } else {
                // surrogate pair
                // int utf32 = pos < end ? (int) data.charAt(pos++) : 0;
                var utf32 = data[pos++].toInt()
                utf32 = (code - 0xD7C0 shl 10) + (utf32 and 0x3FF)
                k2 = (0xff and (0xF0 or (utf32 shr 18))
                        or (0x80 or (utf32 shr 12 and 0x3F) shl 8)
                        or (0x80 or (utf32 shr 6 and 0x3F) shl 16)
                        or (0x80 or (utf32 and 0x3F) shl 24))
                bits = 32
            }
            k1 = k1 or (k2 shl shift)

            // int used_bits = 32 - shift;  // how many bits of k2 were used in k1.
            // int unused_bits = bits - used_bits; //  (bits-(32-shift)) == bits+shift-32  == bits-newshift
            shift += bits
            if (shift >= 32) {
                // mix after we have a complete word
                k1 *= c1
                k1 = k1 shl 15 or (k1 ushr 17) // ROTL32(k1,15);
                k1 *= c2
                h1 = h1 xor k1
                h1 = h1 shl 13 or (h1 ushr 19) // ROTL32(h1,13);
                h1 = h1 * 5 + -0x19ab949c
                shift -= 32
                // unfortunately, java won't let you shift 32 bits off, so we need to check for 0
                k1 = if (shift != 0) {
                    k2 ushr bits - shift // bits used == bits - newshift
                } else {
                    0
                }
                nBytes += 4
            }
        } // inner

        // handle tail
        if (shift > 0) {
            nBytes += shift shr 3
            k1 *= c1
            k1 = k1 shl 15 or (k1 ushr 17) // ROTL32(k1,15);
            k1 *= c2
            h1 = h1 xor k1
        }

        // finalization
        h1 = h1 xor nBytes

        // fmix(h1);
        h1 = h1 xor (h1 ushr 16)
        h1 *= -0x7a143595
        h1 = h1 xor (h1 ushr 13)
        h1 *= -0x3d4d51cb
        h1 = h1 xor (h1 ushr 16)

        return h1
    }
}
