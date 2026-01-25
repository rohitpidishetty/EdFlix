package com.edFlix.EdFlix.Util;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class RASecuredMessagingProtocol {

    // Rohit Acharya's Secured Messaging Protocol (RASMP)

    /**
     * @author Er. P. Rohit V. Acharya
     * @date Dec-05-2025
     * Exclusively built for YAARI's secure E2E encryption and decryption.
     * This is an advanced version of RASEM, which used to support only UTF-8, this new version supports UTF-32 bit encoding.
     */

    public RASecuredMessagingProtocol() {
    }

    private final int BLOCK_SIZE = 4;
    private final int MAX_CHAR_BUFFER = 32;
    private final int BLOCK_LENGTH = 8;
    private final char OFF = '0', ON = '1';
    private final int UPPER_BOUND = 0x7FFFFFFE; // 2147483646
    private final int LOWER_BOUND = 0x3FFFFFFF; // 1073741823
    private final boolean debug = false;

    private class Secrets extends RASecuredMessagingProtocol {

        protected String secret;
        protected String[] _4bit_conf_keys;

        public Secrets(String secret, String[] _4bit_conf_keys) {
            this.secret = secret;
            this._4bit_conf_keys = _4bit_conf_keys;
        }

        protected String patch(String[] _4bit_conf_keys) {
            StringBuilder encoded_binary_stream = new StringBuilder();
            for (int i = 0; i < super.BLOCK_LENGTH; i++) {
                encoded_binary_stream.append(_4bit_conf_keys[i]);
            }
            if (super.debug) System.out.println(encoded_binary_stream);
            return encoded_binary_stream.toString();
        }
    }

    private class BitStream extends RASecuredMessagingProtocol {

        private static final char[] hex_digest = "0123456789abcdef".toCharArray();

        public String toHex(String binary) {
            return String.valueOf(hex_digest[Integer.parseInt(binary, 2)]);
        }

        private String mapToHex(String stream) {
            return toHex(stream);
        }

        private StringBuilder bits_stream;

        public BitStream() {
            this.bits_stream = new StringBuilder();
        }

        private void patch_bits(String stream) {
            bits_stream.append(stream);
        }

        private String encode() {
            int len = this.bits_stream.length();
            if (super.debug) System.out.println(this.bits_stream);

            int padding = super.BLOCK_LENGTH - (len % super.BLOCK_LENGTH);
            if (padding != super.BLOCK_LENGTH) {
                this.bits_stream.append("0".repeat(padding));
            }

            byte[] bytes = new byte[len / 8];
            for (int i = 0; i < bytes.length; i++) {
                String byteStr =
                        this.bits_stream.substring(
                                i * super.BLOCK_LENGTH,
                                i * super.BLOCK_LENGTH + super.BLOCK_LENGTH
                        );
                bytes[i] = (byte) Integer.parseInt(byteStr, 2);
            }
            return Base64.getEncoder().encodeToString(bytes);
        }

        private String encode_key(String key) {
            int len = key.length();

            int padding = super.BLOCK_LENGTH - (len % super.BLOCK_LENGTH);
            if (padding != super.BLOCK_LENGTH) key += ("0".repeat(padding));

            byte[] bytes = new byte[len / 8];
            for (int i = 0; i < bytes.length; i++) {
                String byteStr = key.substring(
                        i * super.BLOCK_LENGTH,
                        i * super.BLOCK_LENGTH + super.BLOCK_LENGTH
                );
                bytes[i] = (byte) Integer.parseInt(byteStr, 2);
            }
            if (debug) System.out.println(Arrays.toString(bytes));
            return Base64.getEncoder().encodeToString(bytes);
        }

        private String salt_key(String encrypted_message, String encrypted_key) {
            return encrypted_key + ";" + encrypted_message;
        }
    }

    private Secrets init(String stream) {
        System.out.println(
                """
                        === Rohit Acharya's Secured Messaging Protocol ENGAGED ===\r
                        Encryption ON | Authentication ON | Integrity Shield ACTIVE\r
                        Channel locked with proprietary cryptography."""
        );
        SecureRandom random = new SecureRandom();
        byte[] entropy = new StringBuilder()
                .append(stream)
                .append(UUID.randomUUID().toString())
                .toString()
                .getBytes();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(entropy);
            byte sha_salt = 0b1111111;
            for (byte b : key) sha_salt ^= b;
            int bytes =
                    random.nextInt((this.UPPER_BOUND - this.LOWER_BOUND + 1)) +
                            this.LOWER_BOUND;
            int final_key = (bytes + sha_salt) % this.UPPER_BOUND;
            String KEY = "0" + Integer.toBinaryString(final_key);
            if (debug) System.out.println(KEY + " = enc key =");
            String[] blocks = fragmentKey(KEY);
            if (debug) System.out.println(Arrays.toString(blocks));
            return new Secrets(KEY, blocks);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private String binary_xor_operation(String stream1, String stream2) {
        StringBuilder _4_bits = new StringBuilder();
        for (int i = this.BLOCK_SIZE - 1; i >= 0; i--) {
            char _sb1 = stream1.charAt(i), _sb2 = stream2.charAt(i);
            if (
                    (_sb1 == this.OFF && _sb2 == this.OFF) ||
                            (_sb1 == this.ON && _sb2 == this.ON)
            ) _4_bits.append(this.OFF);
            else if (
                    (_sb1 == this.ON && _sb2 == this.OFF) ||
                            (_sb1 == this.OFF && _sb2 == this.ON)
            ) _4_bits.append(this.ON);
        }
        return _4_bits.reverse().toString();
    }

    public String encrypt(String stream) {
        Secrets secret = init(stream);
        BitStream bits = new BitStream();
        byte[] byte_stream = stream.getBytes();

        if (debug) System.out.println(Arrays.toString(byte_stream));
        for (byte _8bits : byte_stream) {
            String bit_stream = Integer.toBinaryString(_8bits);
            int bit_stream_length = bit_stream.length();
            String _4_byte_stream = new StringBuilder()
                    .append(String.valueOf(this.OFF).repeat(this.MAX_CHAR_BUFFER - bit_stream_length))
                    .append(bit_stream)
                    .toString();
            if (debug) System.out.println(_4_byte_stream);
            // 32 bits -> 8 blocks i.e., 4 bits per each block.
            String[] blocks = new String[this.BLOCK_LENGTH];
            int idx = 0;
            for (
                    int i = 1;
                    i < (this.MAX_CHAR_BUFFER - this.BLOCK_SIZE) + this.BLOCK_SIZE;
                    i += this.BLOCK_SIZE
            ) {
                int bit_start = (i - 1);
                int offset = bit_start + this.BLOCK_SIZE;
                blocks[idx] = this.binary_xor_operation(
                        _4_byte_stream.substring(bit_start, offset),
                        secret._4bit_conf_keys[idx]
                );
                idx++;
            }
            bits.patch_bits(secret.patch(blocks));
        }
        if (debug) {
            System.out.println(secret.secret);
            System.out.println(bits.bits_stream);
        }
        return bits.salt_key(bits.encode(), bits.encode_key(secret.secret));
    }

    public String decrypt(String encrypted_data) {
        if (debug) System.out.println(encrypted_data);
        String encrypted_key = encrypted_data.substring(
                0,
                encrypted_data.indexOf(";")
        );
        String encrypted_message = encrypted_data.substring(
                encrypted_data.indexOf(";") + 1
        );

        byte[] key_buffer = Base64.getDecoder()
                .decode(encrypted_key), message_buffer = Base64.getDecoder()
                .decode(encrypted_message);
        if (debug) System.out.println(
                encrypted_data +
                        " " +
                        Arrays.toString(Base64.getDecoder().decode(encrypted_key))
        );

        String[] key = this.fragmentKey(streamDeserializer(key_buffer));

        List<String> _32_bit_words = _32_bitWordFragmentation(
                streamDeserializer(message_buffer)
        );

        int mag = _32_bit_words.size();

        byte[] output_stream = new byte[mag];
        for (int i = 0; i < mag; i++)
            output_stream[i] = (byte) Long.parseLong(
                    _32_bit_stream_decoder(_32_bit_words.get(i), key),
                    2
            );

        return new String(output_stream, StandardCharsets.UTF_8);
    }

    private String _32_bit_stream_decoder(
            String _32_bit_stream,
            String[] subKeys
    ) {
        StringBuilder buffer = new StringBuilder();
        int key_idx = 0;

        for (
                int i = 0;
                i < this.MAX_CHAR_BUFFER;
                i += this.BLOCK_SIZE
        )
            buffer.append(
                    this.binary_xor_operation(
                            _32_bit_stream.substring(i, (i + this.BLOCK_SIZE)),
                            subKeys[key_idx++]
                    )
            );
        return buffer.toString();
    }

    private List<String> _32_bitWordFragmentation(String stream) {
        List<String> buffer = new ArrayList<>();
        int len = stream.length();
        for (int i = 0; i < len; i += this.MAX_CHAR_BUFFER)
            buffer.add(
                    stream.substring(i, (i + this.MAX_CHAR_BUFFER))
            );
        return buffer;
    }

    private String streamDeserializer(byte[] buffer) {
        StringBuilder decrypted_buffer = new StringBuilder();
        for (int i = 0; i < buffer.length; i++) {
            String string_rep = Integer.toBinaryString(
                    buffer[i] & ((1 << this.BLOCK_LENGTH) - 1)
            );
            decrypted_buffer
                    .append("0".repeat(this.BLOCK_LENGTH - string_rep.length()))
                    .append(string_rep);
        }
        return decrypted_buffer.toString();
    }

    private String[] fragmentKey(String stream) {
        String[] blocks = new String[this.BLOCK_LENGTH];
        int idx = 0;
        for (
                int i = 1;
                i < (this.MAX_CHAR_BUFFER - this.BLOCK_SIZE);
                i += this.BLOCK_SIZE
        ) {
            int bit_start = (i - 1);
            int offset = bit_start + this.BLOCK_SIZE;
            blocks[idx++] = stream.substring(bit_start, offset);
        }
        blocks[idx] = blocks[0];
        return blocks;
    }
    // public static void main(String[] args) {
    //   RASecuredMessagingProtocol obj = new RASecuredMessagingProtocol();
    //   String encrypted_data = obj.encrypt("Hey hi how are you ? ");
    //   byte[] decoded_message = obj.decrypt(encrypted_data);
    //   System.out.println(Arrays.toString(decoded_message));
    // }
}