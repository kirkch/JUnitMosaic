package com.softwaremosaic.junit.quickcheck;

import com.softwaremosaic.junit.lang.Function0;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import net.java.quickcheck.generator.distribution.Distribution;
import net.java.quickcheck.generator.support.*;

import java.util.HashMap;
import java.util.Map;


/**
 *
 */
@SuppressWarnings("unchecked")
public class GeneratorFactory {

    private static final Map<Class,Function0<Generator>> DEFAULT_GENERATORS = new HashMap<>();

    static {
        DEFAULT_GENERATORS.put( Boolean.class, new Function0() {
            public Generator<Boolean> invoke() {
                return PrimitiveGenerators.booleans();
            }
        } );

        DEFAULT_GENERATORS.put( Boolean.TYPE, new Function0() {
            public Generator<Boolean> invoke() {
                return PrimitiveGenerators.booleans();
            }
        } );

        DEFAULT_GENERATORS.put( Byte.class, new Function0() {
            public Generator<Byte> invoke() {
                return PrimitiveGenerators.bytes();
            }
        } );

        DEFAULT_GENERATORS.put( Byte.TYPE, new Function0() {
            public Generator<Byte> invoke() {
                return PrimitiveGenerators.bytes();
            }
        } );

        DEFAULT_GENERATORS.put( Character.class, new Function0() {
            public Generator<Character> invoke() {
                return new CharacterGenerator();
            }
        } );

        DEFAULT_GENERATORS.put( Character.TYPE, new Function0() {
            public Generator<Character> invoke() {
                return new CharacterGenerator();
            }
        } );

        DEFAULT_GENERATORS.put( Short.class, new Function0() {
            public Generator<Short> invoke() {
                return new ShortGenerator();
            }
        } );

        DEFAULT_GENERATORS.put( Short.TYPE, new Function0() {
            public Generator<Short> invoke() {
                return new ShortGenerator();
            }
        } );


        DEFAULT_GENERATORS.put( Integer.class, new Function0() {
            public Generator<Integer> invoke() {
                return new IntegerGenerator();
            }
        } );

        DEFAULT_GENERATORS.put( Integer.TYPE, new Function0() {
            public Generator<Integer> invoke() {
                return new IntegerGenerator();
            }
        } );


        DEFAULT_GENERATORS.put( Long.class, new Function0() {
            public Generator<Long> invoke() {
                return new LongGenerator();
            }
        } );

        DEFAULT_GENERATORS.put( Long.TYPE, new Function0() {
            public Generator<Long> invoke() {
                return new LongGenerator();
            }
        } );

        DEFAULT_GENERATORS.put( Float.class, new Function0() {
            public Generator<Float> invoke() {
                return new FloatGenerator();
            }
        } );

        DEFAULT_GENERATORS.put( Float.TYPE, new Function0() {
            public Generator<Float> invoke() {
                return new FloatGenerator();
            }
        } );

        DEFAULT_GENERATORS.put( Double.class, new Function0() {
            public Generator<Double> invoke() {
                return PrimitiveGenerators.doubles();
            }
        } );

        DEFAULT_GENERATORS.put( Double.TYPE, new Function0() {
            public Generator<Double> invoke() {
                return PrimitiveGenerators.doubles();
            }
        } );

        DEFAULT_GENERATORS.put( String.class, new Function0() {
            public Generator<String> invoke() {
                return PrimitiveGenerators.strings();
            }
        } );
    }




    private Distribution                    distribution;
    private Map<Class,Function0<Generator>> generators   = new HashMap<>();


    public GeneratorFactory() {
        this( Distribution.UNIFORM );
    }

    public GeneratorFactory( Distribution d ) {
        this.distribution = d;
        this.generators   = new HashMap( DEFAULT_GENERATORS );
    }

    public <T> void register( Class<T> type, Function0<Generator<T>> factory ) {
        generators.put( type, (Function0) factory );
    }


    public <T> Generator<T> newGeneratorFor( Class<T> type ) {
        if ( type.isArray() ) {
            return new ArrayGenerator( 0, 10000, type.getComponentType(), distribution, this );
        } else {
            Function0<Generator> factory = generators.get(type);

            return factory.invoke();
        }
    }

}
