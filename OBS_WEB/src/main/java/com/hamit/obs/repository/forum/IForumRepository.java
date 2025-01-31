package com.hamit.obs.repository.forum;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hamit.obs.custom.degiskenler.UygulamaSabitleri;
import com.hamit.obs.dto.forum.Commit;
import com.hamit.obs.dto.forum.Subjects;
import com.hamit.obs.exception.ServiceException;
@Component
public class IForumRepository  {

	@SuppressWarnings("unused")
	public void createForum() {
		String sqlSubjects = "CREATE TABLE subjects ("
				+ "    subjectid SERIAL PRIMARY KEY,"
				+ "    subjecttitle VARCHAR(100) NOT NULL,"
				+ "    subjectdescription VARCHAR(300),"
				+ "    createdat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ "    createdby VARCHAR(100) NOT NULL);" ;

		String sqlCommits = "CREATE TABLE commits ("
				+ "    commitid SERIAL PRIMARY KEY,"
				+ "    subjectid INT NOT NULL ,"
				+ "    committext VARCHAR(300),"
				+ "    createdby VARCHAR(100) NOT NULL,"
				+ "    createdat TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

		String sqlmesajsayiString = "CREATE TABLE mesajsayi("
				+ "    mesajsayiid SERIAL PRIMARY KEY ,"
				+ "	   username VARCHAR(100) NOT NULL,"
				+ "    mesajsayi integer);";
	}

	public void subjectSave(String subjectTitle, String subjectDescription,String CreatedBy) {
		String sql = "INSERT INTO subjects (subjecttitle, subjectdescription,createdby) VALUES (?, ?,?)";
		try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, subjectTitle);
			preparedStatement.setString(2, subjectDescription);
			preparedStatement.setString(3, CreatedBy);
			preparedStatement.executeUpdate();
			mesajsayiUpdate(CreatedBy,1);
		} catch (SQLException e) {
			throw new ServiceException( e.getMessage()); 
		}
	}
	public void commitSave(Long subjectID, String commitText, String createdBy) {
		String sql = "INSERT INTO commits (subjectid, committext, createdby) VALUES (?, ?, ?)";
		try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setLong(1, subjectID);
			preparedStatement.setString(2, commitText);
			preparedStatement.setString(3, createdBy);
			preparedStatement.executeUpdate();
			mesajsayiUpdate(createdBy,1);
		} catch (SQLException e) {
			throw new ServiceException( e.getMessage()); 
		}
	}

	public void mesajsayiSaveUser(String username) {
		String sql = "INSERT INTO mesajsayi (username,mesajsayi) VALUES (?,0)";
		try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, username);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException( e.getMessage()); 
		}
	}
	public void mesajsayiUpdate(String username,int sayi) {
		String sql = "UPDATE mesajsayi SET mesajsayi = mesajsayi + ? WHERE username != ? ";
		try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, sayi);
			preparedStatement.setString(2, username);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException( e.getMessage()); 
		}
	}

	public void mesajsayiUpdateUser(String username) {
		String sql = "UPDATE mesajsayi SET mesajsayi = 0 WHERE username = ? ";
		try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, username);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException( e.getMessage()); 
		}
	}

	public int getmesajsayi(String username) {
		String sql = "SELECT mesajsayi FROM mesajsayi WHERE username = ?";
		try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, username); 
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt("mesajsayi");
			}
		} catch (SQLException e) {
			throw new ServiceException( e.getMessage());  
		}
	}

	public List<Subjects> findAllWithCommits() throws Exception {
		List<Subjects> subjects = new ArrayList<>();
		Map<Long, Subjects> subjectMap = new HashMap<>();
		String sql = "SELECT s.subjectid, s.subjecttitle, s.subjectdescription, s.createdby, " +
				"c.commitID, c.committext, c.createdby AS CommitCreatedBy " +
				"FROM subjects s " +
				"LEFT JOIN commits c ON s.subjectid = c.subjectid " +
				"ORDER BY s.subjectid";
		try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString, UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				Long subjectId = resultSet.getLong("subjectid");
				Subjects subject = subjectMap.get(subjectId);
				if (subject == null) {
					subject = new Subjects();
					subject.setSubjectID(subjectId);
					subject.setSubjectTitle(resultSet.getString("subjecttitle"));
					subject.setSubjectDescription(resultSet.getString("subjectdescription"));
					subject.setCreatedBy(resultSet.getString("createdby"));

					subject.setCommits(subject.getCommits() != null ? subject.getCommits() : new ArrayList<>());
					subjects.add(subject);
					subjectMap.put(subjectId, subject);
				}
				long commitId = resultSet.getInt("commitid");
				if (commitId > 0) {
					Commit commit = new Commit();
					commit.setCommitID(commitId);
					commit.setSubjectID(subjectId);
					commit.setCommitText(resultSet.getString("committext"));
					commit.setCreatedBy(resultSet.getString("commitcreatedby"));
					subject.getCommits().add(commit);
				}
			}
		} catch (SQLException e) {
			throw new ServiceException( e.getMessage()); 
		}
		return subjects;
	}
	
	public void mesajsayiDeleteUser(String username) {
		String sql = "DELETE FROM mesajsayi WHERE username = ?";
		try (Connection connection = DriverManager.getConnection(UygulamaSabitleri.forumConnString,UygulamaSabitleri.FORUMUSER_STRING, UygulamaSabitleri.FORUMPWD_STRING);
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, username);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new ServiceException( e.getMessage()); 
		}
	}
}