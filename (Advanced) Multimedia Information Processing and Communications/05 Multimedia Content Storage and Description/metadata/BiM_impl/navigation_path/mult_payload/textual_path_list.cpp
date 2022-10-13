#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <string.h>
#include "textual_path_list.h"

int		textual_path_list::m_num;
char*	textual_path_list::m_list[MAX_LENGTH];

void textual_path_list::clear(void){
	int	i;

	for(i=0; i<MAX_LENGTH; i++){
		if(m_list[i] != NULL){
			free(m_list[i]);
			m_list[i] = NULL;
		}
	}
	
	m_num = 0;
}

void textual_path_list::add(const char *textual_path){
	size_t	length;

	length = strlen(textual_path);

	m_list[m_num] = (char *)calloc(length + 1, sizeof(char));
	strncpy(m_list[m_num], textual_path, length + 1);

	m_num++;
}

int textual_path_list::get_num(void){

	return m_num;
}

char **textual_path_list::get_list(void){

	return m_list;
}
