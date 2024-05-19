#ifndef TEXTUAL_PATH_LIST_H
#define TEXTUAL_PATH_LIST_H

#define MAX_LENGTH	10000

class textual_path_list{
private:
	static int	m_num;
	static char	*m_list[MAX_LENGTH];
public:
	static void clear(void);
	static void	add(const char *textual_path);
	static int	get_num(void);
	static char **get_list(void);
};

#endif /* TEXTUAL_PATH_LIST_H */