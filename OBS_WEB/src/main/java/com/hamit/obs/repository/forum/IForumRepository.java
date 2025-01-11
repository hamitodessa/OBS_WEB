package com.hamit.obs.repository.forum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.custom.degiskenler.UygulamaSabitleri;
import com.hamit.obs.model.forum.Subjects;
import com.hamit.obs.model.forum.Commit;
@Component
public class IForumRepository  {

	@SuppressWarnings("unused")
	public void createForum() {
		String sqlSubjects = "CREATE TABLE Subjects ("
				+ "    SubjectID SERIAL PRIMARY KEY,"
				+ "    SubjectTitle VARCHAR(255) NOT NULL,"
				+ "    SubjectDescription TEXT,"
				+ "    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ "    CreatedBy VARCHAR(255) NOT NULL);" ;
		
		String sqlCommits = "CREATE TABLE Commits ("
				+ "    CommitID SERIAL PRIMARY KEY,"
				+ "    SubjectID INT NOT NULL REFERENCES Subjects(SubjectID) ON DELETE CASCADE,"
				+ "    CommitText TEXT NOT NULL,"
				+ "    CreatedBy VARCHAR(100) NOT NULL,"
				+ "    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
	}
	
	public void subjectSave(String subjectTitle, String subjectDescription,String CreatedBy) {
	    String sql = "INSERT INTO Subjects (SubjectTitle, SubjectDescription,CreatedBy) VALUES (?, ?,?)";
	    try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
	         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
	        preparedStatement.setString(1, subjectTitle);
	        preparedStatement.setString(2, subjectDescription);
	        preparedStatement.setString(3, CreatedBy);
	        preparedStatement.executeUpdate();
	    } catch (SQLException e) {
	        System.err.println("Subject kaydı sırasında hata oluştu.");
	    }
	}
	
	public void commitSave(Long subjectID, String commitText, String createdBy) {
	    String sql = "INSERT INTO Commits (SubjectID, CommitText, CreatedBy) VALUES (?, ?, ?)";
	    try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
	         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
	        preparedStatement.setLong(1, subjectID);
	        preparedStatement.setString(2, commitText);
	        preparedStatement.setString(3, createdBy);
	        preparedStatement.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public List<Commit> getCommitsBySubjectID(Long subjectID) {
        List<Commit> commits = new ArrayList<>();
        String sql = "SELECT c.CommitID, c.CommitText, c.CreatedBy, c.CreatedAt, c.CreatedBy FROM Commits c WHERE c.SubjectID = ?";
        try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, subjectID); // Parametreyi ayarla
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Commit commit = new Commit();
                    commit.setCommitID(resultSet.getLong("CommitID"));
                    commit.setCommitText(resultSet.getString("CommitText"));
                    commit.setCreatedBy(resultSet.getString("CreatedBy"));
                    commit.setCreatedAt(resultSet.getTimestamp("CreatedAt"));
                    commits.add(commit); // Listeye ekle
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commits;
    }
	
	public List<Subjects> getAllSubjects() {
        List<Subjects> subjects = new ArrayList<>();
        String sql = "SELECT SubjectID, SubjectTitle, SubjectDescription, CreatedAt,CreatedBy FROM Subjects";
        try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
            	Subjects subject = new Subjects();
                subject.setSubjectID(resultSet.getLong("SubjectID"));
                subject.setSubjectTitle(resultSet.getString("SubjectTitle"));
                subject.setSubjectDescription(resultSet.getString("SubjectDescription"));
                subject.setCreatedAt(resultSet.getTimestamp("CreatedAt"));
                subject.setCreatedBy(resultSet.getString("CreatedBy"));
                subjects.add(subject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }
	
	public List<Subjects> findAllWithCommits() throws Exception {
	    List<Subjects> subjects = new ArrayList<>();
	    Map<Long, Subjects> subjectMap = new HashMap<>();
	    String sql = "SELECT s.SubjectID, s.SubjectTitle, s.SubjectDescription, s.CreatedBy, " +
	            "c.CommitID, c.CommitText, c.CreatedBy AS CommitCreatedBy " +
	            "FROM Subjects s " +
	            "LEFT JOIN Commits c ON s.SubjectID = c.SubjectID " +
	            "ORDER BY s.SubjectID";
	    try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString, UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
	         PreparedStatement statement = connection.prepareStatement(sql);
	         ResultSet resultSet = statement.executeQuery()) {
	        while (resultSet.next()) {
	            Long subjectId = resultSet.getLong("SubjectID");
	            Subjects subject = subjectMap.get(subjectId);

	            if (subject == null) {
	                subject = new Subjects();
	                subject.setSubjectID(subjectId);
	                subject.setSubjectTitle(resultSet.getString("SubjectTitle"));
	                subject.setSubjectDescription(resultSet.getString("SubjectDescription"));
	                subject.setCreatedBy(resultSet.getString("CreatedBy"));
	                
	                // Eğer commits null ise yeni bir liste ata
	                subject.setCommits(subject.getCommits() != null ? subject.getCommits() : new ArrayList<>());
	                
	                subjects.add(subject);
	                subjectMap.put(subjectId, subject);
	            }

	            long commitId = resultSet.getInt("CommitID");
	            if (commitId > 0) {
	                Commit commit = new Commit();
	                commit.setCommitID(commitId);
	                commit.setSubjectID(subjectId);
	                commit.setCommitText(resultSet.getString("CommitText"));
	                commit.setCreatedBy(resultSet.getString("CommitCreatedBy"));
	                subject.getCommits().add(commit);
	            }
	        }
	    }
	    return subjects;
	}
}

