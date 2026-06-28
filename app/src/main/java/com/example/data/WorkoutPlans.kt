package com.example.data

data class Exercise(
    val name: String,
    val category: String, // Chest, Back, Legs, Shoulders, Arms, Core, Cardio
    val defaultSets: Int = 4,
    val defaultReps: String = "10-12",
    val defaultRestSecs: Int = 60,
    val videoUrl: String = "https://www.youtube.com/results?search_query=how+to+do+exercise",
    val instructions: String = "Perform with slow controlled motion. Focus on mind-muscle connection."
)

data class WorkoutPlan(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "Gym", "Home"
    val split: String, // "Push Pull Legs", "Bro Split", "Full Body", "Upper Lower"
    val goal: String, // "Muscle Gain", "Fat Loss", "Strength Training"
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val exercises: List<Exercise>,
    val durationMinutes: Int,
    val estCaloriesBurned: Int
)

object WorkoutPlans {
    val plans = listOf(
        WorkoutPlan(
            id = "gym_ppl_push",
            title = "Push Day (Chest, Shoulders, Triceps)",
            description = "High-intensity compound push routines designed for strength and muscle growth.",
            type = "Gym",
            split = "Push Pull Legs",
            goal = "Muscle Gain",
            level = "Intermediate",
            durationMinutes = 60,
            estCaloriesBurned = 450,
            exercises = listOf(
                Exercise("Barbell Bench Press", "Chest", 4, "8-10", 90, "https://www.youtube.com/watch?v=gRVjAtPip0Y", "Keep feet planted, bar to mid-chest level, press up dynamically."),
                Exercise("Incline Dumbbell Press", "Chest", 3, "10-12", 60, "https://www.youtube.com/watch?v=28SgZ-pW2E0", "Set bench to 30 degrees, control descent, press up fully."),
                Exercise("Overhead Press (OHP)", "Shoulders", 4, "8", 90, "https://www.youtube.com/watch?v=QAQ64hK4DNs", "Squeeze core, press bar straight up, clear the chin."),
                Exercise("Lateral Raises", "Shoulders", 3, "12-15", 45, "https://www.youtube.com/watch?v=3VcKaX_pG-I", "Lead with elbows, slight forward lean, pause at peak."),
                Exercise("Tricep Overhead Extensions", "Arms", 3, "10-12", 60, "https://www.youtube.com/watch?v=nRiJVZD5_04", "Keep elbows tucked in near ears, full stretch at bottom.")
            )
        ),
        WorkoutPlan(
            id = "gym_ppl_pull",
            title = "Pull Day (Back, Biceps)",
            description = "Hypertrophy routine focusing on the entire posterior chain and elbow flexors.",
            type = "Gym",
            split = "Push Pull Legs",
            goal = "Muscle Gain",
            level = "Intermediate",
            durationMinutes = 55,
            estCaloriesBurned = 420,
            exercises = listOf(
                Exercise("Deadlift", "Back", 4, "5", 120, "https://www.youtube.com/watch?v=op9kVnSso6Q", "Keep flat back, bar close to shins, drive through heels."),
                Exercise("Lat Pulldown", "Back", 4, "10-12", 60, "https://www.youtube.com/watch?v=CAwf7n6Luuc", "Pull bar to upper chest, squeeze shoulder blades together."),
                Exercise("Barbell Row", "Back", 3, "8-10", 60, "https://www.youtube.com/watch?v=FWJR5Ve8qnQ", "Bent over at 45 degrees, pull bar to lower stomach."),
                Exercise("Incline Dumbbell Curl", "Arms", 3, "12", 60, "https://www.youtube.com/watch?v=soxrdi1qxeU", "Slightly behind shoulders, fully isolate biceps."),
                Exercise("Hammer Curls", "Arms", 3, "12", 45, "https://www.youtube.com/watch?v=twWMCVMeLIg", "Keep elbows pinned, neutral grip, squeeze forearm and bicep.")
            )
        ),
        WorkoutPlan(
            id = "gym_ppl_legs",
            title = "Leg Day (Squats, Hamstrings, Calves)",
            description = "Intense lower body session targeting quads, glutes, hamstrings, and calves.",
            type = "Gym",
            split = "Push Pull Legs",
            goal = "Muscle Gain",
            level = "Intermediate",
            durationMinutes = 65,
            estCaloriesBurned = 550,
            exercises = listOf(
                Exercise("Barbell Back Squat", "Legs", 4, "6-8", 120, "https://www.youtube.com/watch?v=SW_C1A-rejs", "Saddle hips back, drop thighs parallel to ground, stand up."),
                Exercise("Romanian Deadlift (RDL)", "Legs", 3, "10", 90, "https://www.youtube.com/watch?v=JCXUYuzw01M", "Hinge from hips, feel deep stretch in hamstrings, pull back up."),
                Exercise("Leg Press", "Legs", 3, "12", 60, "https://www.youtube.com/watch?v=yZMx_Acf_T8", "Lower knees safely to 90 degrees, do not lock out knees at top."),
                Exercise("Lying Leg Curls", "Legs", 3, "12", 45, "https://www.youtube.com/watch?v=1Tq3QdYVv1s", "Isolate hamstrings, keep hips flat on bench."),
                Exercise("Standing Calf Raises", "Legs", 4, "15", 45, "https://www.youtube.com/watch?v=3UWi44yN-wM", "Full stretch at bottom, explosive drive, hold contraction.")
            )
        ),
        WorkoutPlan(
            id = "gym_bro_chest",
            title = "Bro Split (Chest Devastation)",
            description = "High-volume classic chest specialization day for maximal hypertrophy.",
            type = "Gym",
            split = "Bro Split",
            goal = "Muscle Gain",
            level = "Advanced",
            durationMinutes = 50,
            estCaloriesBurned = 400,
            exercises = listOf(
                Exercise("Incline Barbell Bench Press", "Chest", 4, "8", 90, "https://www.youtube.com/watch?v=SrqOu55i-Ux", "Target upper fibers. Control decline, touch upper collarbone."),
                Exercise("Flat Dumbbell Press", "Chest", 4, "10", 60, "https://www.youtube.com/watch?v=VM7H2Z8S3S8", "Push up in arc, avoid touching dumbbells at peak."),
                Exercise("Dips (Weighted or Chest-focused)", "Chest", 3, "12", 60, "https://www.youtube.com/watch?v=yN6Q1UI_xkE", "Lean forward, flare elbows slightly to isolate lower chest."),
                Exercise("Cable Crossovers", "Chest", 3, "15", 45, "https://www.youtube.com/watch?v=W_7v8_zP6hA", "Squeeze at center, keep minor elbow bend, control return.")
            )
        ),
        WorkoutPlan(
            id = "home_full_body",
            title = "Home Full Body Calisthenics",
            description = "No equipment required. High-intensity bodyweight movements for endurance and fat loss.",
            type = "Home",
            split = "Full Body",
            goal = "Fat Loss",
            level = "Beginner",
            durationMinutes = 35,
            estCaloriesBurned = 300,
            exercises = listOf(
                Exercise("Push-Ups", "Chest", 4, "Max", 45, "https://www.youtube.com/watch?v=IODxDxX7oi4", "Keep rigid plank shape, elbows tucked 45 degrees, chest to floor."),
                Exercise("Bodyweight Squats", "Legs", 4, "20", 45, "https://www.youtube.com/watch?v=UI6H_gXg_lM", "Focus on depth, squeeze glutes at top."),
                Exercise("Doorway Rows / Towel Rows", "Back", 3, "12-15", 45, "https://www.youtube.com/watch?v=x7fGq0nI0q0", "Using stable anchor, pull body weight up controls."),
                Exercise("Pike Pushups", "Shoulders", 3, "8-10", 60, "https://www.youtube.com/watch?v=sposDXWEB0A", "Elevate hips, lower forehead towards floor diagonally."),
                Exercise("Plank to Elbows", "Core", 3, "45s", 30, "https://www.youtube.com/watch?v=TvxNkmjdhMM", "Engage core, alternate resting on forearms and hands.")
            )
        ),
        WorkoutPlan(
            id = "strength_upper_lower",
            title = "Strength Focused: Upper Body Power",
            description = "Lower volume, heavy compound lift focus designed for absolute neural strength gains.",
            type = "Gym",
            split = "Upper Lower",
            goal = "Strength Training",
            level = "Advanced",
            durationMinutes = 60,
            estCaloriesBurned = 380,
            exercises = listOf(
                Exercise("Heavy Flat Bench Press", "Chest", 5, "5", 150, "https://www.youtube.com/watch?v=gRVjAtPip0Y", "Focus on power. Lift with explosive ascent and controlled descent."),
                Exercise("Weighted Pullups", "Back", 4, "6", 120, "https://www.youtube.com/watch?v=7uDclVq_Uis", "Full range, chest to bar, add external load if possible."),
                Exercise("Standing Military Barbell Press", "Shoulders", 4, "5", 120, "https://www.youtube.com/watch?v=QAQ64hK4DNs", "Brace core, squeeze glutes, press straight up overhead."),
                Exercise("Barbell Pendlay Rows", "Back", 3, "6", 90, "https://www.youtube.com/watch?v=h4n_8E8994g", "Pull from dead-stop on floor, torso parallel to ground.")
            )
        )
    )
}
