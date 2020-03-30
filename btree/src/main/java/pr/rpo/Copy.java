package pr.rpo;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * when use this class, should be very very carefule
 *
 * some problem:
 * final field
 *
 */
public class Copy {

    public static Object deepCopy(Object object) {
        try {
            if(object.getClass().isArray()) {
                int arrayLen = Array.getLength(object);
                Object newArrayObj = Array.newInstance(object.getClass().getComponentType(), arrayLen);
                for(int i=0; i<arrayLen; i++) {
                    Array.set(newArrayObj,i,Array.get(object,i));
                }

                return newArrayObj;
            }else {
                Constructor constructor = object.getClass().getDeclaredConstructor();
                constructor.setAccessible(true);
                Object newObj = constructor.newInstance();

                for (Field f : object.getClass().getDeclaredFields()) {
                    if(Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }

                    f.setAccessible(true);
                    if (isPrimitiveAndPackageType(f.getType())) {
                        if (f.getType().isPrimitive()) {
                            switch (f.getType().getTypeName()) {
                                case "boolean":
                                    f.setBoolean(newObj,f.getBoolean(object));break;
                                case "int":
                                    f.setInt(newObj,f.getInt(object));break;
                                case "float":
                                    f.setFloat(newObj,f.getFloat(object));break;
                                case "double":
                                    f.setDouble(newObj,f.getDouble(object));break;
                                case "char":
                                    f.setChar(newObj,f.getChar(object));break;
                                case "byte":
                                    f.setByte(newObj,f.getByte(object));break;
                                case "short":
                                    f.setShort(newObj,f.getShort(object));break;
                                default:;
                            }
                        } else {
                            if (f.get(object) != null) {
                                f.set(newObj, f.getType().getConstructor().newInstance());
                                f.set(newObj, f.get(object));
                            }
                        }
                    } else {
                        if (f.get(object) != null) {
                            f.set(newObj, deepCopy(f.get(object)));
                        }
                    }
                }
                return newObj;
            }
        }catch (Exception e) {
            e.printStackTrace();;
        }
        return null;
    }


    private static boolean isPrimitiveAndPackageType(Class<?> targetClazz) {
        if(targetClazz.isPrimitive() == true) {
            return true;
        }

        if (Number.class.isAssignableFrom(targetClazz)) {
            return true;
        }

        if (targetClazz == Boolean.class || targetClazz == Character.class || targetClazz == Void.class) {
            return true;
        }
        return false;
    }
}
