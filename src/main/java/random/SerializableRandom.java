package random;

import java.io.Serializable;

public class SerializableRandom implements Serializable {
    // Serializable을 구현하여 Kryo가 쉽게 처리하도록 함
    private static final long SERIAL_VERSION_UID = 1L;

    private long seed;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public SerializableRandom() {}
    public SerializableRandom(long seed) {
        // 생성 시 seed 설정
        this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
    }

    /**
     * 난수 생성을 위한 핵심 메서드. 지정된 비트 수만큼의 난수를 생성합니다.
     * @param bits (1 ~ 32)
     * @return 생성된 난수 비트
     */
    private int next(int bits) {
        seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        return (int) (seed >>> (48 - bits));
    }

    /**
     * 0부터 (bound - 1) 사이의 int 난수를 반환합니다.
     * @param bound 경계값 (양수여야 함)
     * @return 생성된 난수
     */
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }

        // 통계적 편향을 없애기 위한 알고리즘 (java.util.Random 방식)
        int r = next(31);
        int m = bound - 1;
        if ((bound & m) == 0) { // bound가 2의 거듭제곱일 경우
            r = (int) ((bound * (long) r) >> 31);
        } else {
            for (int u = r; u - (r = u % bound) + m < 0; u = next(31));
        }
        return r;
    }

    /**
     * long 타입의 전체 범위 내에서 난수를 반환합니다.
     * @return 생성된 long 난수
     */
    public long nextLong() {
        // 32비트 난수 두 개를 조합하여 64비트 long을 생성
        return ((long) next(32) << 32) + next(32);
    }

    /**
     * 0부터 (bound - 1) 사이의 long 난수를 반환합니다.
     * @param bound 경계값 (양수여야 함)
     * @return 생성된 long 난수
     */
    public long nextLong(long bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }

        long r = nextLong();
        long m = bound - 1;

        // 통계적 편향을 없애기 위한 알고리즘
        if ((bound & m) == 0) { // bound가 2의 거듭제곱일 경우
            r = r & m;
        } else {
            for (long u = r >>> 1; u + m - (r = u % bound) < 0; u = nextLong() >>> 1);
        }
        return r;
    }
}