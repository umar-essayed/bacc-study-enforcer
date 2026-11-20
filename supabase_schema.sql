-- ====================================================================
-- Egyptian Baccalaureate Study Enforcer - Supabase Database Schema
-- Track: 2nd Year Secondary (Engineering & CS Track)
-- Subjects: Arabic Nahw (النحو), English, History (تاريخ), Programming & AI
-- ====================================================================

-- 1. Create Questions Table
CREATE TABLE IF NOT EXISTS public.questions (
    id BIGSERIAL PRIMARY KEY,
    subject TEXT NOT NULL CHECK (subject IN ('ARABIC', 'ENGLISH', 'HISTORY', 'PROGRAMMING')),
    topic TEXT NOT NULL,
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option_index SMALLINT NOT NULL CHECK (correct_option_index BETWEEN 0 AND 3),
    explanation TEXT NOT NULL,
    language_rtl BOOLEAN DEFAULT TRUE,
    difficulty TEXT DEFAULT 'MEDIUM' CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 2. Indexes for Ultra-Fast Retrieval and Sync
CREATE INDEX IF NOT EXISTS idx_questions_subject ON public.questions(subject);
CREATE INDEX IF NOT EXISTS idx_questions_topic ON public.questions(topic);
CREATE INDEX IF NOT EXISTS idx_questions_created_at ON public.questions(created_at DESC);

-- 3. Row Level Security (RLS)
ALTER TABLE public.questions ENABLE ROW LEVEL SECURITY;

-- Allow anonymous read access for app synchronization
DROP POLICY IF EXISTS "Public Read Access for Questions" ON public.questions;
CREATE POLICY "Public Read Access for Questions"
    ON public.questions
    FOR SELECT
    TO anon, authenticated
    USING (true);

-- Allow authenticated admins to insert/update questions
DROP POLICY IF EXISTS "Admin Insert Access for Questions" ON public.questions;
CREATE POLICY "Admin Insert Access for Questions"
    ON public.questions
    FOR ALL
    TO authenticated
    USING (true)
    WITH CHECK (true);

-- 4. Initial Sample Remote Seeds
INSERT INTO public.questions (subject, topic, question_text, option_a, option_b, option_c, option_d, correct_option_index, explanation, language_rtl, difficulty)
VALUES
('ARABIC', 'إعراب المضارع', 'ما إعراب الفعل (ينجحَ) في جملة: «اجتهدْ في دروسك فتنجحَ»؟', 'مضارع مرفوع بالضمة', 'مضارع منصوب بفاء السببية بالفتحة الظاهرة', 'مضارع مجزوم في جواب الطلب', 'مضارع مبني على الفتح', 1, 'سُبق الفعل بفاء السببية المسبوقة بطلب (أمر: اجتهدْ)، فينصب المضارع بعدها بالفتحة الظاهرة.', true, 'MEDIUM'),
('ARABIC', 'الأسماء الخمسة', 'أي الجمل الآتية كُتبت بصورة نحوية صحيحة وفق قواعد الأسماء الخمسة؟', 'إنّ ذا الخلقِ الكريمِ محبوبٌ', 'إنّ ذو الخلقِ الكريمِ محبوبٌ', 'إنّ ذي الخلقِ الكريمِ محبوباً', 'إنّ ذوي الخلق محبوب', 0, '«ذا» اسم إن منصوب بالألف لأنه من الأسماء الخمسة وخبرها «محبوبٌ» مرفوع.', true, 'EASY'),
('ENGLISH', 'Conditionals', 'If the algorithm ________ optimized yesterday, the runtime would be substantially faster today.', 'was', 'had been', 'has been', 'would have been', 1, 'Mixed conditional: Past condition affecting the present result.', false, 'MEDIUM'),
('PROGRAMMING', 'Time Complexity', 'What is the worst-case time complexity of Binary Search on a sorted array of N elements?', 'O(1)', 'O(N)', 'O(log N)', 'O(N log N)', 2, 'Binary search divides the search space in half at each step, yielding logarithmic O(log N) time.', false, 'EASY'),
('HISTORY', 'عصر محمد علي', 'ما السياسة الاقتصادية التي طبقها محمد علي لتنظيم الإنتاج والتحكم في تجارة مصر الخارجية؟', 'نظام الالتزام', 'نظام الاحتكار', 'نظام الإقطاع', 'حرية التجارة المفتوحة', 1, 'طبق محمد علي نظام الاحتكار ليضمن الدخول في سوق التجارة الدولية منافساً لغيره من الدول.', true, 'EASY');
