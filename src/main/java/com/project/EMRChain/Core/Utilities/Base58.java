package com.project.EMRChain.Core.Utilities;


/**
*   Base58 is a way to encode addresses (or arbitrary data) as alphanumeric strings.
*
*   The basic idea of the encoding is to:
*   - Treat the data bytes as a large number represented using base-256 digits,
*   - Convert the number to be represented using base-58 digits,
*   - Preserve the exact number of leading zeros (which are otherwise lost during the mathematical operations on the numbers),
*   - And finally represent the resulting base-58 digits as alphanumeric ASCII characters.
*/


public class Base58
{
    public static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int BASE_58 = ALPHABET.length; // 58 ~ Used for getting remainder
    private static final char ENCODED_ZERO = ALPHABET[0];
    private static final int BASE_256 = 256;
    private static final int[] INDEXES = new int[128];

    static
    {
        for (int i=0; i<INDEXES.length; i++)
        {
            INDEXES[i] = -1;
        }

        for (int i=0; i<ALPHABET.length; i++)
        {
            INDEXES[ALPHABET[i]] = i;
        }
    }


    /**
    *  Encodes a byte[] as a Base58 String.
    *  @param input the bytes to encode
    *  @return the base58-encoded string
    */
    public static String encode(byte[] input)
    {
        if (input.length == 0) {
            return "";
        }

        // Count the leading zeros
        int zerosCount = 0;
        while (zerosCount < input.length && input[zerosCount] == 0) {
            zerosCount++;
        }

                // -- Convert base-256 digits to base-58 digits (plus conversion to ASCII characters) -- \\

        // Create a copy of the input, since we modify it in-place.
        input = copyOfRange(input, 0, input.length);

                                                // -- Encode -- \\
        byte[] temp = new byte[input.length * 2]; // temp array with double the size if input (upper bound ~encoded)
        int j = temp.length;
        int startAt = zerosCount; // Skip the zeros (start at where the zeroes end)

        while (startAt < input.length)
        {
            byte mod = divmod58(input, startAt);

            if (input[startAt] == 0) {
                startAt++; // Skip leading zeros
            }

            temp[j--] = (byte) ALPHABET[mod];
        }

        // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
        while (j < temp.length && temp[j] == ENCODED_ZERO) {
            j++;
        }

        // Add as many leading '1' as there were leading zeros.
        while (zerosCount-- >= 0) {
            temp[j--] = (byte) ENCODED_ZERO;
        }

        byte[] output = copyOfRange(temp, j, temp.length);

        return new String(output);
    }


    /**
    * Decodes the given base58 string into the original data bytes.
    * @param input the base58-encoded string to decode
    * @return the decoded data bytes
    */
    public static byte[] decode(String input)
    {
        if (input.length() == 0) {
            return new byte[0];
        }

        byte[] input58 = new byte[input.length()];

        // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
        for (int i = 0; i < input.length(); ++i)
        {
            char c = input.charAt(i);

            int digit58 = -1;
            if (c >= 0 && c < 128) {
                digit58 = INDEXES[c];
            }
            if (digit58 < 0) {
                throw new RuntimeException("Not a Utils.Base58 input: " + input);
            }

            input58[i] = (byte) digit58;
        }

        // Count leading zeroes
        int zeroCount = 0;
        while (zeroCount < input58.length && input58[zeroCount] == 0) {
            ++zeroCount;
        }

        // Convert base-58 digits to base-256 digits.
        byte[] temp = new byte[input.length()];
        int j = temp.length;

        int startAt = zeroCount;
        while (startAt < input58.length)
        {
            byte mod = divmod256(input58, startAt);
            if (input58[startAt] == 0) {
                ++startAt;
            }

            temp[--j] = mod;
        }

        // Do no add extra leading zeroes, move j to first non null byte.
        while (j < temp.length && temp[j] == 0) {
            ++j;
        }

        return copyOfRange(temp, j - zeroCount, temp.length);
    }

    // Copes values from source array into a new array and returns it
    private static byte[] copyOfRange(byte[] source, int from, int to)
    {
        byte[] range = new byte[to-from];
        System.arraycopy(source, from, range, 0, range.length); // Faster than Arrays.copyOf
        return range;
    }

    /*
    * 'divmod' Divides a number, represented as an array of bytes each containing a single digit
    * in the specified base, by the given divisor. The given number is modified in-place
    * to contain the quotient, and the return value is the remainder.
    */

    // Returns number % 58 remainder, used for encoding
    @SuppressWarnings("Duplicates")
    private static byte divmod58(byte[] number, int startAt)
    {
        int remainder = 0;

        for (int i=startAt; i<number.length; i++)
        {
            // -----------------------------------------------------------------------------------------
            // "& 0xff” effectively masks the variable so it leaves only the value in the last 8 bits,
            // and ignores all the rest of the bits.
            // https://android.jlelse.eu/java-when-to-use-n-8-0xff-and-when-to-use-byte-n-8-2efd82ae7dd7
            // -----------------------------------------------------------------------------------------
            int digit256 = (int) number[i] & 0xFF;

            int temp = remainder * BASE_256 + digit256;

            number[i] = (byte) (temp / BASE_58);

            remainder = temp % BASE_58;
        }

        return (byte) remainder;
    }

    // Returns number % 256 remainder, used for decoding
    @SuppressWarnings("Duplicates")
    private static byte divmod256(byte[] number58, int startAt)
    {
        int remainder = 0;

        for (int i = startAt; i < number58.length; i++)
        {
            int digit58 = (int) number58[i] & 0xFF;
            int temp = remainder * BASE_58 + digit58;

            number58[i] = (byte) (temp / BASE_256);

            remainder = temp % BASE_256;
        }

        return (byte) remainder;
    }
}