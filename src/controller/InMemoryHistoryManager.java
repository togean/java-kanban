package controller;

import models.Node;
import models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> historyArrayListOfTasks = new ArrayList<>();
    private final linkedListOfTasks linkedListOfTasks = new linkedListOfTasks();
    private final HashMap<Integer, Node<Task>> hashmapOfTasksInHistory = new HashMap<>();

    @Override
    public List<Task> getHistory() {
        historyArrayListOfTasks.clear();//Очищаем ранее существовавший список
        Node<Task> currentNode = linkedListOfTasks.first;
        if(currentNode!=null) {
            historyArrayListOfTasks.add(currentNode.getNodeItem());
            while (currentNode.getNexItem() != null) {
                currentNode = currentNode.getNexItem();
                historyArrayListOfTasks.add(currentNode.getNodeItem());
            }
        }
        return historyArrayListOfTasks;
    }

    @Override
    public void add(Task task) {
        if(hashmapOfTasksInHistory.containsKey(task.getId())){
            remove(task.getId());//Если такая задача в истории уже была, то удаляем её
        }
        if(linkedListOfTasks.first == null){//Если в списке нет задач или была одна, но она на предыдущем шаге была удалена, т.к. ранее уже была в истории
            linkedListOfTasks.first = new Node<>(task, null, null);
            linkedListOfTasks.last = null;
            hashmapOfTasksInHistory.put(task.getId(), linkedListOfTasks.first);
        }else{

            Node<Task> prevNode;
            if(linkedListOfTasks.last == null) {
                prevNode = linkedListOfTasks.first;
            }else{
                prevNode = linkedListOfTasks.last;
            }
            Node<Task> nodeToCreate = new Node<>(task, null, prevNode);
            linkedListOfTasks.last = nodeToCreate;
            prevNode.setNexItem(nodeToCreate);
            linkedListOfTasks.last.setPrevItem(prevNode);
            hashmapOfTasksInHistory.put(task.getId(), linkedListOfTasks.last);
        }
    }
    @Override
    public void remove(int id){
        if(hashmapOfTasksInHistory.containsKey(id)) {//Проверяем, что удаляемая задача есть в мапе
            if(hashmapOfTasksInHistory.get(id).getNexItem()!=null&&hashmapOfTasksInHistory.get(id).getPrevItem()!=null){
                hashmapOfTasksInHistory.get(id).getPrevItem().setNexItem(hashmapOfTasksInHistory.get(id).getNexItem());
                hashmapOfTasksInHistory.get(id).getNexItem().setPrevItem(hashmapOfTasksInHistory.get(id).getPrevItem());
                hashmapOfTasksInHistory.remove(id);
                return;
            }
            if(hashmapOfTasksInHistory.get(id).getNexItem()==null&&hashmapOfTasksInHistory.get(id).getPrevItem()!=null){
                hashmapOfTasksInHistory.get(id).getPrevItem().setNexItem(null);
                linkedListOfTasks.last = hashmapOfTasksInHistory.get(id).getPrevItem();
                hashmapOfTasksInHistory.remove(id);
                return;
            }
            if(hashmapOfTasksInHistory.get(id).getNexItem()!=null&&hashmapOfTasksInHistory.get(id).getPrevItem()==null){
                hashmapOfTasksInHistory.get(id).getNexItem().setPrevItem(null);
                linkedListOfTasks.first = hashmapOfTasksInHistory.get(id).getNexItem();
                hashmapOfTasksInHistory.remove(id);
                return;
            }
            if(hashmapOfTasksInHistory.get(id).getNexItem()==null&&hashmapOfTasksInHistory.get(id).getPrevItem()==null){
                linkedListOfTasks.first=null;
                linkedListOfTasks.last=null;
                hashmapOfTasksInHistory.clear();
                return;
            }
        }
    }
    static class linkedListOfTasks{
        Node<Task> first;
        Node<Task> last;
    }
}
