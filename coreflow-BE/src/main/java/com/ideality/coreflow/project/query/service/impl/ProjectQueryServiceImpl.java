package com.ideality.coreflow.project.query.service.impl;

import com.ideality.coreflow.common.exception.BaseException;
import com.ideality.coreflow.common.exception.ErrorCode;
import com.ideality.coreflow.project.command.domain.aggregate.Project;
import com.ideality.coreflow.project.command.domain.aggregate.Work;
import com.ideality.coreflow.project.query.dto.CompletedProjectDTO;
import com.ideality.coreflow.project.query.dto.DetailDTO;
import com.ideality.coreflow.project.query.dto.NodeDTO;
import com.ideality.coreflow.project.query.dto.ProjectDetailResponseDTO;
import com.ideality.coreflow.project.query.dto.PipelineResponseDTO;
import com.ideality.coreflow.project.query.dto.ProjectSummaryDTO;
import com.ideality.coreflow.project.query.dto.RequestDeptDTO;
import com.ideality.coreflow.project.query.dto.UserInfoDTO;
import com.ideality.coreflow.project.query.dto.WorkDeptDTO;
import com.ideality.coreflow.project.query.mapper.ProjectMapper;
import com.ideality.coreflow.project.query.service.ProjectQueryService;
import com.ideality.coreflow.project.query.service.WorkQueryService;
import com.ideality.coreflow.template.query.dto.DeptDTO;
import com.ideality.coreflow.template.query.dto.EdgeDTO;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProjectQueryServiceImpl implements ProjectQueryService {

    private final ProjectMapper projectMapper;
    private final WorkQueryService workQueryService;

    @Override
    public List<ProjectSummaryDTO> selectProjectSummaries(Long userId) {
        return projectMapper.selectParticipatingProjects(userId);
    }

    @Override
    public ProjectDetailResponseDTO getProjectDetail(Long projectId) {
        ProjectDetailResponseDTO projectDetail = projectMapper.getProjectDetail(projectId);
        List<UserInfoDTO> leaders = projectMapper.getProjectLeaders(projectId);
        ProjectDetailResponseDTO result = ProjectDetailResponseDTO.builder()
                .id(projectDetail.getId())
                .name(projectDetail.getName())
                .description(projectDetail.getDescription())
                .createdDate(projectDetail.getCreatedDate())
                .startBase(projectDetail.getStartBase())
                .endBase(projectDetail.getEndBase())
                .startExpect(projectDetail.getStartExpect())
                .endExpect(projectDetail.getEndExpect())
                .startReal(projectDetail.getStartReal())
                .endReal(projectDetail.getEndReal())
                .progressRate(projectDetail.getProgressRate())
                .passedRate(projectDetail.getPassedRate())
                .delayDays(projectDetail.getDelayDays())
                .status(projectDetail.getStatus())
                .director(projectDetail.getDirector())
                .leaders(leaders)
                .build();
        return result;
    }

    @Override
    public PipelineResponseDTO getPipeline(Long projectId) {
        PipelineResponseDTO baseProject = projectMapper.findProjectById(projectId);
        List<NodeDTO> works = projectMapper.findWorksByProjectId(projectId);
        List<EdgeDTO> edges = projectMapper.findRelationsByProjectId(projectId);

        List<Long> workIds = works.stream().map(NodeDTO::getId).collect(Collectors.toList());
        List<WorkDeptDTO> allDeptMappings = projectMapper.findDeptsByWorkIds(workIds);

        Map<Long, List<DeptDTO>> workIdToDeptList = allDeptMappings.stream()
                .collect(Collectors.groupingBy(
                        WorkDeptDTO::getTaskId,
                        Collectors.mapping(dto -> new DeptDTO(dto.getDeptId(), dto.getDeptName()), Collectors.toList())
                ));

        List<NodeDTO> nodesWithDepts = works.stream()
                .map(node -> NodeDTO.builder()
                        .id(node.getId())
                        .name(node.getName())
                        .description(node.getDescription())
                        .startBase(node.getStartBase())
                        .endBase(node.getEndBase())
                        .startExpect(node.getStartExpect())
                        .endExpect(node.getEndExpect())
                        .startReal(node.getStartReal())
                        .endReal(node.getEndReal())
                        .progressRate(node.getProgressRate())
                        .passedRate(node.getPassedRate())
                        .delayDays(node.getDelayDays())
                        .status(node.getStatus())
                        .slackTime(node.getSlackTime())
                        .warning(node.getWarning())
                        .deptList(workIdToDeptList.getOrDefault(node.getId(), List.of()))
                        .build())
                .toList();

        return PipelineResponseDTO.builder()
                .name(baseProject.getName())
                .description(baseProject.getDescription())
                .startBase(baseProject.getStartBase())
                .endBase(baseProject.getEndBase())
                .startExpect(baseProject.getStartExpect())
                .endExpect(baseProject.getEndExpect())
                .startReal(baseProject.getStartReal())
                .endReal(baseProject.getEndReal())
                .progressRate(baseProject.getProgressRate())
                .passedRate(baseProject.getPassedRate())
                .delayDays(baseProject.getDelayDays())
                .status(baseProject.getStatus())
                .nodeList(nodesWithDepts)
                .edgeList(edges)
                .build();
    }

//    private Boolean checkTaskWarning(NodeDTO task) {
//        LocalDate taskEndExpect = task.getEndExpect();
//        System.out.println("task.getName() = " + task.getName());
//        System.out.println("taskEndExpect = " + taskEndExpect);
//        List<Work> subTasks = workQueryService.getSubTasksByParentTaskId(task.getId());
//        for (Work subTask : subTasks) {
//            if (taskEndExpect.isBefore(subTask.getEndExpect())) {
//                return true;
//            }
//        }
//        return false;
//    }

    // 완료 상태 프로젝트 가져오기
    @Override
    public List<CompletedProjectDTO> selectCompletedProjects() {
        List<CompletedProjectDTO> list = projectMapper.selectCompletedProjects();

        if (list == null || list.isEmpty()) {
            throw new BaseException(ErrorCode.NO_COMPLETED_PROJECT);
        }

        return list;
    }

    @Override
    public ProjectSummaryDTO selectProjectSummary(Long projectId) {
        return projectMapper.selectProjectSummary(projectId);
    }

    @Override
    public String getProjectName(Long projectId) {
        return projectMapper.selectProjectNameByProjectId(projectId);
    }

    @Override
    public List<ProjectSummaryDTO> selectProjectByDeptId(Long deptId) {
        return projectMapper.selectProjectByDeptId(deptId);
    }

    @Override
    public String findProjectNameByProjectId(Long projectId) {
        return projectMapper.findProjectNameByProjectId(projectId);
    }

    @Override
    public List<Long> getProjectIdsInProgressByUser(Long userId) {
        return projectMapper.findOngoingProjectIdsByUser(userId);
    }
}
