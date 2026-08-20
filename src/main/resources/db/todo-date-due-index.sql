-- Partial index for minute-range todo reminder polling.
-- Hibernate ddl-auto cannot express WHERE completed = false AND notified_at IS NULL.

CREATE INDEX IF NOT EXISTS idx_todo_date_due
    ON public.todo_date (remind_at)
    WHERE completed = false AND notified_at IS NULL;;

-- Existing rows: compute remind_at from date + todo.todo_time in KST.
UPDATE public.todo_date td
SET remind_at = timezone('Asia/Seoul', (td.date + t.todo_time))
FROM public.todo t
WHERE td.todo_id = t.todo_id
  AND td.remind_at IS NULL
  AND t.todo_time IS NOT NULL;;
