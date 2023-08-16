#ifndef _TASK_
#define _TASK_

struct Task_t
{
	using TaskPtr = void (*)();
	const TaskPtr fn;
	inline void run() const
	{
		if (fn != NULL)
			fn();
	}
};

#endif