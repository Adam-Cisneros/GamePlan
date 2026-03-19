package com.twig.gameplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.twig.gameplan.api.GitHubService
import com.twig.gameplan.data.Group
import com.twig.gameplan.data.Plan
import com.twig.gameplan.data.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GamePlanViewModel : ViewModel() {
    val stageList = listOf("To Do", "In Progress", "In Review", "Done")

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String? get() = auth.currentUser?.uid
    
    // GitHub API setup
    private val githubService = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GitHubService::class.java)

    private val _gitHubToken = MutableStateFlow<String?>(null)
    val gitHubToken: StateFlow<String?> = _gitHubToken

    fun setGitHubToken(token: String) {
        _gitHubToken.value = "token $token"
    }

    // --- Task Operations ---
    val allTasks: Flow<List<Task>> = userId?.let { uid ->
        db.collection("tasks")
            .whereEqualTo("uid", uid)
            .dataObjects<Task>()
    } ?: flowOf(emptyList())

    fun getTasksByPlan(planId: String): Flow<List<Task>> {
        return userId?.let { uid ->
            db.collection("tasks")
                .whereEqualTo("uid", uid)
                .whereEqualTo("planId", planId)
                .dataObjects<Task>()
        } ?: flowOf(emptyList())
    }

    fun addTask(task: Task) {
        val uid = userId ?: return
        viewModelScope.launch {
            db.collection("tasks").add(task.copy(uid = uid)).await()
        }
    }

    fun updateTask(task: Task) {
        val uid = userId ?: return
        if (task.uid != uid) return // Security check
        viewModelScope.launch {
            db.collection("tasks").document(task.id).set(task).await()
        }
    }

    fun deleteTask(task: Task) {
        val uid = userId ?: return
        if (task.uid != uid) return
        viewModelScope.launch {
            db.collection("tasks").document(task.id).delete().await()
        }
    }

    fun moveTaskStage(task: Task, direction: Int) {
        val currentIndex = stageList.indexOf(task.stage)
        val newIndex = currentIndex + direction
        if (newIndex < 0 || newIndex >= stageList.size) return

        updateTask(task.copy(stage = stageList[newIndex]))
    }

    // --- Plan Operations ---
    val allPlans: Flow<List<Plan>> = userId?.let { uid ->
        db.collection("plans")
            .whereEqualTo("uid", uid)
            .dataObjects<Plan>()
    } ?: flowOf(emptyList())

    fun getPlanById(planId: String): Flow<Plan?> {
        return userId?.let { uid ->
            db.collection("plans")
                .document(planId)
                .dataObjects<Plan>()
                .map { if (it?.uid == uid) it else null }
        } ?: flowOf(null)
    }

    fun getPlansByGroup(groupId: String): Flow<List<Plan>> {
        return userId?.let { uid ->
            db.collection("plans")
                .whereEqualTo("uid", uid)
                .whereEqualTo("groupId", groupId)
                .dataObjects<Plan>()
        } ?: flowOf(emptyList())
    }

    fun addPlan(plan: Plan) {
        val uid = userId ?: return
        viewModelScope.launch {
            db.collection("plans").add(plan.copy(uid = uid)).await()
        }
    }

    fun updatePlan(plan: Plan) {
        val uid = userId ?: return
        if (plan.uid != uid) return
        viewModelScope.launch {
            db.collection("plans").document(plan.id).set(plan).await()
        }
    }

    fun deletePlan(plan: Plan) {
        val uid = userId ?: return
        if (plan.uid != uid) return
        viewModelScope.launch {
            db.collection("plans").document(plan.id).delete().await()
        }
    }

    // --- Group Operations ---
    val allGroups: Flow<List<Group>> = userId?.let { uid ->
        db.collection("groups")
            .whereEqualTo("uid", uid)
            .dataObjects<Group>()
    } ?: flowOf(emptyList())

    fun getGroupById(groupId: String): Flow<Group?> {
        return userId?.let { uid ->
            db.collection("groups")
                .document(groupId)
                .dataObjects<Group>()
                .map { if (it?.uid == uid) it else null }
        } ?: flowOf(null)
    }

    fun addGroup(group: Group) {
        val uid = userId ?: return
        viewModelScope.launch {
            db.collection("groups").add(group.copy(uid = uid)).await()
        }
    }

    fun updateGroup(group: Group) {
        val uid = userId ?: return
        if (group.uid != uid) return
        viewModelScope.launch {
            db.collection("groups").document(group.id).set(group).await()
        }
    }

    fun deleteGroup(group: Group) {
        val uid = userId ?: return
        if (group.uid != uid) return
        viewModelScope.launch {
            db.collection("groups").document(group.id).delete().await()
        }
    }

    // --- GitHub Import Operations ---

    suspend fun getGitHubRepos() = gitHubToken.value?.let { githubService.getUserRepos(it) } ?: emptyList()
    suspend fun getGitHubOrgs() = gitHubToken.value?.let { githubService.getUserOrgs(it) } ?: emptyList()
    suspend fun getGitHubIssues(owner: String, repo: String) = gitHubToken.value?.let { githubService.getRepoIssues(it, owner, repo) } ?: emptyList()

    fun importGitHubRepoAsPlan(repoName: String, owner: String) {
        val token = gitHubToken.value ?: return
        val uid = userId ?: return
        viewModelScope.launch {
            val repos = githubService.getUserRepos(token)
            val repo = repos.find { it.name == repoName && it.owner.login == owner } ?: return@launch
            
            val newPlan = Plan(
                title = repo.name,
                body = repo.description,
                uid = uid
            )
            val planRef = db.collection("plans").add(newPlan).await()
            val planId = planRef.id

            // Import Issues as Tasks
            val issues = githubService.getRepoIssues(token, owner, repoName)
            issues.forEach { issue ->
                val newTask = Task(
                    title = issue.title,
                    body = issue.body,
                    planId = planId,
                    uid = uid,
                    milestoneTitle = issue.milestone?.title,
                    stage = if (issue.state == "closed") "Done" else "To Do"
                )
                db.collection("tasks").add(newTask).await()
            }
        }
    }

    fun importGitHubOrgAsGroup(orgName: String) {
        val token = gitHubToken.value ?: return
        val uid = userId ?: return
        viewModelScope.launch {
            val orgs = githubService.getUserOrgs(token)
            val org = orgs.find { it.login == orgName } ?: return@launch
            val newGroup = Group(
                title = org.login,
                description = org.description,
                uid = uid
            )
            db.collection("groups").add(newGroup).await()
        }
    }

    fun importGitHubIssueAsTask(owner: String, repo: String, issueTitle: String, planId: String?) {
        val token = gitHubToken.value ?: return
        val uid = userId ?: return
        viewModelScope.launch {
            val issues = githubService.getRepoIssues(token, owner, repo)
            val issue = issues.find { it.title == issueTitle } ?: return@launch
            val newTask = Task(
                title = issue.title,
                body = issue.body,
                planId = planId,
                uid = uid,
                milestoneTitle = issue.milestone?.title,
                stage = if (issue.state == "closed") "Done" else "To Do"
            )
            db.collection("tasks").add(newTask).await()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GamePlanViewModel() as T
            }
        }
    }
}
