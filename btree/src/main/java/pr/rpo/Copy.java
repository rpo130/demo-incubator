package pr.rpo;

import java.lang.reflect.Field;

public class Copy {

    public static Object deepCopy(Object object) {
        try {
            Object newObj = object.getClass().getConstructor().newInstance();

            for (Field f : object.getClass().getDeclaredFields()) {
                if (isPrimitiveAndPackageType(f.getType())) {
                    f.setAccessible(true);
                    if (f.getType().isPrimitive()) {
                        f.set(newObj, f.get(object));
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

        }catch (Exception e) {
            e.printStackTrace();;
        }
        return null;
    }


    static boolean isPrimitiveAndPackageType(Class<?> targetClazz) {
        if(targetClazz.isPrimitive() == true) {
            return true;
        }

        // 判断包装类
        if (Number.class.isAssignableFrom(targetClazz)) {
            return true;
        }
        // 判断原始类,过滤掉特殊的基本类型
        if (targetClazz == Boolean.class || targetClazz == Character.class || targetClazz == Void.class) {
            return true;
        }
        return false;
    }
}
