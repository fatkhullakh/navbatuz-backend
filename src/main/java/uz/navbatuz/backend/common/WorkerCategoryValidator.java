package uz.navbatuz.backend.common;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class WorkerCategoryValidator {
    private static final Map<Category, Set<WorkerType>> compatibilityMap = new EnumMap<>(Category.class);

    static {
        compatibilityMap.put(Category.BARBERSHOP, EnumSet.of(WorkerType.BARBER, WorkerType.HAIRDRESSER));
        compatibilityMap.put(Category.DENTAL, EnumSet.of(WorkerType.DENTIST));
        compatibilityMap.put(Category.CLINIC, EnumSet.of(WorkerType.DOCTOR, WorkerType.NURSER));
        compatibilityMap.put(Category.SPA, EnumSet.of(WorkerType.SPA_THERAPIST, WorkerType.MASSEUSE));
        compatibilityMap.put(Category.GYM, EnumSet.of(WorkerType.PERSONAL_TRAINER));
        compatibilityMap.put(Category.NAIL_SALON, EnumSet.of(WorkerType.NAIL_TECHNICIAN));
        compatibilityMap.put(Category.BEAUTY_CLINIC, EnumSet.of(WorkerType.COSMETOLOGIST));
        compatibilityMap.put(Category.TATTOO_STUDIO, EnumSet.of(WorkerType.TATTOO_ARTIST));
        compatibilityMap.put(Category.MASSAGE_CENTER, EnumSet.of(WorkerType.MASSEUSE));
        compatibilityMap.put(Category.PHYSIOTHERAPY_CLINIC, EnumSet.of(WorkerType.PHYSIOTHERAPIST));
        compatibilityMap.put(Category.MAKEUP_STUDIO, EnumSet.of(WorkerType.MAKEUP_ARTIST));
        compatibilityMap.put(Category.OTHER, EnumSet.allOf(WorkerType.class)); // fallback
    }

    public static boolean isCompatible(Category category, WorkerType workerType) {
        return compatibilityMap.getOrDefault(category, EnumSet.noneOf(WorkerType.class)).contains(workerType);
    }
}
