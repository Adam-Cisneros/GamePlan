package com.twig.gameplan.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

data class GitHubRepo(
    val id: Long,
    val name: String,
    val description: String?,
    val owner: GitHubOwner
)

data class GitHubOwner(
    val login: String
)

data class GitHubOrg(
    val id: Long,
    val login: String,
    val description: String?
)

data class GitHubIssue(
    val id: Long,
    val title: String,
    val body: String?,
    val state: String,
    val milestone: GitHubMilestone?
)

data class GitHubMilestone(
    val title: String
)

data class GitHubProject(
    val id: Long,
    val name: String,
    val body: String?
)

data class GitHubProjectColumn(
    val id: Long,
    val name: String
)

data class GitHubProjectCard(
    val id: Long,
    val note: String?,
    val content_url: String? // URL to the issue or pull request
)

interface GitHubService {
    @GET("user/repos")
    suspend fun getUserRepos(
        @Header("Authorization") token: String
    ): List<GitHubRepo>

    @GET("user/orgs")
    suspend fun getUserOrgs(
        @Header("Authorization") token: String
    ): List<GitHubOrg>

    @GET("repos/{owner}/{repo}/issues")
    suspend fun getRepoIssues(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<GitHubIssue>
}
