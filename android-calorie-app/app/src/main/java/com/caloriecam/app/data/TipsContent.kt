package com.caloriecam.app.data

data class Exercise(val name: String, val detail: String)
data class WorkoutPlan(val emoji: String, val title: String, val subtitle: String, val exercises: List<Exercise>)
data class TipSection(val emoji: String, val title: String, val tips: List<String>)
data class MealFoodItem(val emoji: String, val name: String, val kcal: Int)
data class MealSlot(val time: String, val title: String, val items: List<MealFoodItem>)
data class DailyMenu(val goal: Goal, val meals: List<MealSlot>) {
    val totalKcal: Int get() = meals.sumOf { slot -> slot.items.sumOf { it.kcal } }
}
data class FoodTip(val emoji: String, val name: String, val reason: String)

/** Static, curated advice content — no network/AI calls needed. */
object TipsContent {

    val weightLossTips = TipSection(
        emoji = "🔥",
        title = "Поради для схуднення",
        tips = listOf(
            "Помірний дефіцит калорій — 300–500 ккал на день менше норми. Різкий дефіцит веде до втрати м'язів і зривів.",
            "Більше білка в раціоні (риба, яйця, курка, творог) — довше відчуття ситості, менше м'язів втрачається.",
            "Пийте воду перед їжею та впродовж дня — спрага часто маскується під голод.",
            "Менше рідких калорій: солодкі напої, соки, алкоголь — це калорії без ситості.",
            "Висипайтесь (7–8 год) — нестача сну підвищує гормон голоду (грелін).",
            "Силові тренування 2–3 рази на тиждень зберігають м'язову масу під час дефіциту.",
            "Плануйте прийоми їжі заздалегідь — імпульсивні перекуси найчастіше зривають план.",
            "Фіксуйте кожен прийом їжі (як у цьому застосунку) — сам факт запису знижує переїдання."
        )
    )

    val weightGainTips = TipSection(
        emoji = "💪",
        title = "Поради для набору маси",
        tips = listOf(
            "Помірний профіцит калорій — 300–500 ккал понад норму. Великий надлишок веде переважно до жиру, не м'язів.",
            "1.6–2.2 г білка на кг ваги щодня — основний будівельний матеріал для м'язів.",
            "Силові тренування — головний стимул росту м'язів, без них профіцит просто йде в жир.",
            "Їжте частіше: 4–6 прийомів їжі на день простіше «дотягнути» до денної норми.",
            "Калорійні, але корисні перекуси: горіхи, авокадо, сухофрукти, гранола.",
            "Не нехтуйте вуглеводами — вони дають енергію для важких тренувань.",
            "Достатньо сну (7–9 год) — саме уві сні відбувається основне відновлення й ріст м'язів.",
            "Прогресуйте поступово в тренуваннях (більше ваги/повторень) — м'язи ростуть у відповідь на навантаження."
        )
    )

    val generalNutritionTips = TipSection(
        emoji = "🥗",
        title = "Загальні поради з харчування",
        tips = listOf(
            "Овочі та фрукти — щонайменше 400 г на день, це джерело клітковини й вітамінів.",
            "Обирайте цільнозернові продукти (гречка, вівсянка, бурий рис) замість рафінованих.",
            "Обмежуйте додані цукри та надто оброблену їжу (фастфуд, чіпси, солодощі).",
            "Корисні жири (авокадо, горіхи, оливкова олія, риба) важливі навіть при схудненні.",
            "Регулярність важливіша за ідеальність — краще стабільно дотримуватись плану на 80%, ніж ідеально на 100% три дні."
        )
    )

    val allTipSections = listOf(weightLossTips, weightGainTips, generalNutritionTips)

    val workoutPlans = listOf(
        WorkoutPlan(
            emoji = "🏃",
            title = "Кардіо вдома",
            subtitle = "20 хв, без інвентаря, 3–4 рази на тиждень",
            exercises = listOf(
                Exercise("Розминка", "3 хв легкий біг на місці"),
                Exercise("Біг на місці", "3 підходи по 1 хв, відпочинок 30 сек"),
                Exercise("Стрибки \"джампінг джек\"", "3 підходи по 45 сек"),
                Exercise("Берпі", "3 підходи по 10 повторень"),
                Exercise("Планка", "3 підходи по 30–60 сек"),
                Exercise("Заминка", "3 хв розтяжка")
            )
        ),
        WorkoutPlan(
            emoji = "🏋️",
            title = "Силові без інвентаря",
            subtitle = "30 хв, 2–3 рази на тиждень",
            exercises = listOf(
                Exercise("Присідання", "4 підходи по 15–20 повторень"),
                Exercise("Віджимання від підлоги", "4 підходи по 8–15 повторень"),
                Exercise("Випади на кожну ногу", "3 підходи по 12 повторень"),
                Exercise("Планка", "3 підходи по 45–60 сек"),
                Exercise("Підйом на носочки", "3 підходи по 20 повторень"),
                Exercise("Скручування на прес", "3 підходи по 15–20 повторень")
            )
        ),
        WorkoutPlan(
            emoji = "🧘",
            title = "Розтяжка та відновлення",
            subtitle = "10–15 хв, щодня або після тренування",
            exercises = listOf(
                Exercise("Розтяжка шиї та плечей", "30 сек на кожен бік"),
                Exercise("Нахили до прямих ніг", "3 підходи по 30 сек"),
                Exercise("Розтяжка квадрицепса", "30 сек на кожну ногу"),
                Exercise("Поза \"кішка-корова\"", "1 хв, повільно"),
                Exercise("Глибоке дихання лежачи", "2 хв")
            )
        )
    )

    val weightLossMenu = DailyMenu(
        goal = Goal.LOSE,
        meals = listOf(
            MealSlot(
                "8:00", "Сніданок", listOf(
                    MealFoodItem("🥣", "Вівсяна каша на воді з ягодами", 250),
                    MealFoodItem("🥚", "Яйце варене", 78)
                )
            ),
            MealSlot(
                "11:00", "Перекус", listOf(
                    MealFoodItem("🍎", "Яблуко", 78),
                    MealFoodItem("🥜", "Мигдаль (15 г)", 87)
                )
            ),
            MealSlot(
                "14:00", "Обід", listOf(
                    MealFoodItem("🍗", "Куряче філе гриль (150 г)", 248),
                    MealFoodItem("🥗", "Овочевий салат (200 г)", 90),
                    MealFoodItem("🌾", "Гречка варена (150 г)", 165)
                )
            ),
            MealSlot(
                "17:00", "Перекус", listOf(
                    MealFoodItem("🥛", "Йогурт натуральний (150 г)", 89),
                    MealFoodItem("🥒", "Огірок", 15)
                )
            ),
            MealSlot(
                "19:30", "Вечеря", listOf(
                    MealFoodItem("🐟", "Лосось на грилі (120 г)", 250),
                    MealFoodItem("🥦", "Брокколі на парі (150 г)", 51)
                )
            )
        )
    )

    val maintainMenu = DailyMenu(
        goal = Goal.MAINTAIN,
        meals = listOf(
            MealSlot(
                "8:00", "Сніданок", listOf(
                    MealFoodItem("🍳", "Омлет з 2 яєць", 277),
                    MealFoodItem("🍞", "Хліб цільнозерновий, шматок", 130),
                    MealFoodItem("🥑", "Авокадо (половина)", 120)
                )
            ),
            MealSlot(
                "11:00", "Перекус", listOf(
                    MealFoodItem("🍌", "Банан", 105),
                    MealFoodItem("🥜", "Горіхи (20 г)", 115)
                )
            ),
            MealSlot(
                "14:00", "Обід", listOf(
                    MealFoodItem("🥩", "Яловичина стейк (150 г)", 375),
                    MealFoodItem("🍚", "Рис варений (150 г)", 195),
                    MealFoodItem("🥗", "Овочевий салат (150 г)", 68)
                )
            ),
            MealSlot(
                "17:00", "Перекус", listOf(
                    MealFoodItem("🧀", "Сир кисломолочний (150 г)", 147),
                    MealFoodItem("🍯", "Мед (1 ч.л.)", 40)
                )
            ),
            MealSlot(
                "19:30", "Вечеря", listOf(
                    MealFoodItem("🍗", "Куряче філе (150 г)", 248),
                    MealFoodItem("🥔", "Картопля варена (200 г)", 174),
                    MealFoodItem("🥕", "Овочі на парі (150 г)", 50)
                )
            )
        )
    )

    val weightGainMenu = DailyMenu(
        goal = Goal.GAIN,
        meals = listOf(
            MealSlot(
                "8:00", "Сніданок", listOf(
                    MealFoodItem("🥣", "Вівсяна каша, велика порція (350 г)", 238),
                    MealFoodItem("🍌", "Банан", 105),
                    MealFoodItem("🥜", "Арахісове масло (30 г)", 170),
                    MealFoodItem("🥛", "Молоко (250 мл)", 105)
                )
            ),
            MealSlot(
                "11:00", "Перекус", listOf(
                    MealFoodItem("🥣", "Гранола (80 г)", 377),
                    MealFoodItem("🥛", "Йогурт (150 г)", 89)
                )
            ),
            MealSlot(
                "14:00", "Обід", listOf(
                    MealFoodItem("🥩", "Яловичина (200 г)", 500),
                    MealFoodItem("🍚", "Рис варений (200 г)", 260),
                    MealFoodItem("🥑", "Авокадо (половина)", 160)
                )
            ),
            MealSlot(
                "17:00", "Перекус", listOf(
                    MealFoodItem("🥜", "Волоські горіхи (40 г)", 262),
                    MealFoodItem("🍌", "Банан", 105)
                )
            ),
            MealSlot(
                "19:30", "Вечеря", listOf(
                    MealFoodItem("🐟", "Лосось на грилі (200 г)", 416),
                    MealFoodItem("🥔", "Картопля варена (250 г)", 218),
                    MealFoodItem("🥦", "Брокколі (150 г)", 51)
                )
            )
        )
    )

    val dailyMenus = listOf(weightLossMenu, maintainMenu, weightGainMenu)

    val weightLossFoods = listOf(
        FoodTip("🥦", "Брокколі", "Дуже мало калорій, багато клітковини — довго насичує"),
        FoodTip("🍗", "Куряче філе", "Пісний білок, зберігає м'язи під час дефіциту"),
        FoodTip("🥚", "Яйця", "Дешеве повноцінне джерело білка, насичує надовго"),
        FoodTip("🫐", "Ягоди", "Солодкі, але низькокалорійні — заміна десертам"),
        FoodTip("🥑", "Авокадо", "Корисні жири в невеликій порції дають ситість"),
        FoodTip("🐟", "Риба (лосось, тунець)", "Білок + омега-3, мало вуглеводів"),
        FoodTip("🥒", "Огірки й салат", "Майже нульова калорійність, додають об'єму їжі"),
        FoodTip("🍵", "Зелений чай", "Без калорій, гарна альтернатива солодким напоям")
    )
}
