

#include <stdio.h>

typedef struct node_tag node_t;
typedef struct linked_list_tag linked_list;

struct node_tag {
    node_t *prev;
    int value;
    node_t *next;
};

struct linked_list_tag {
    node_t *head;
    int len;
};

// void add_node(node_t node, int idx) {
//     // TODO: this is where we get into malloc/free
// }

int main() {
    // printf() displays the string inside quotation
    printf("Hello, World!\n");

    // Not dynamic yet. Just hard coding the linked list nodes 
    // to begin with. Focusing on pointers at first before malloc.
    node_t n1, n2, n3;
    n1.prev = NULL;
    n1.value = 37;
    n1.next = &n2;

    n2.prev = &n1;
    n2.value = 38;
    n2.next = &n3;

    n3.prev = &n2;
    n3.value = 39;
    n3.next = NULL;

    node_t *p1 = &n1;

    // practicing member access
    while (1) {
        printf("%d\n", p1->value);
        if (p1->next == NULL) {
            break;
        } else {
            p1 = p1->next;
        };
    }

    while (1) {
        printf("%d\n", p1->value);
        if (p1->prev == NULL) {
            break;
        } else {
            p1 = p1->prev;
        };
    }


    return 0;
}


