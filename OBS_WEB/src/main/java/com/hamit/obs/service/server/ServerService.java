package com.hamit.obs.service.server;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.hamit.obs.createnewDB.createMSSQL;
import com.hamit.obs.createnewDB.createMYSQL;
import com.hamit.obs.createnewDB.createPGSQL;
import com.hamit.obs.custom.enums.sqlTipi;
import com.hamit.obs.dto.server.serverBilgiDTO;

@Service
public class ServerService {


	public boolean serverKontrol(serverBilgiDTO serverBilgiDTO)
	{
		boolean result = false;
		sqlTipi sqTipi = sqlTipi.fromString(serverBilgiDTO.getHangi_sql());
		switch (sqTipi) {
        case MSSQL -> {
            createMSSQL createMSSQL = new createMSSQL();
            result = createMSSQL.serverKontrol(serverBilgiDTO);
        }
        case MYSQL -> {
            createMYSQL createMYSQL = new createMYSQL();
            result = createMYSQL.serverKontrol(serverBilgiDTO);
        }
        case PGSQL -> {
            createPGSQL createPGSQL = new createPGSQL();
            result = createPGSQL.serverKontrol(serverBilgiDTO);
        }
    }
		return result;
	}

	public boolean dosyakontrol(serverBilgiDTO serverBilgiDTO)
	{
		boolean result = false;
		sqlTipi sqTipi = sqlTipi.fromString(serverBilgiDTO.getHangi_sql());
		switch (sqTipi) {
        case MSSQL -> {
            createMSSQL createMSSQL = new createMSSQL();
            result = createMSSQL.dosyaKontrol(serverBilgiDTO);
        }
        case MYSQL -> {
            createMYSQL createMYSQL = new createMYSQL();
            result = createMYSQL.dosyaKontrol(serverBilgiDTO);
        }
        case PGSQL -> {
            createPGSQL createPGSQL = new createPGSQL();
            result = createPGSQL.dosyaKontrol(serverBilgiDTO);
        }
    }
		return result;
	}

	public boolean dosyaolustur(serverBilgiDTO serverBilgiDTO)
	{
		boolean result =false;
		sqlTipi sqTipi = sqlTipi.fromString(serverBilgiDTO.getHangi_sql());
		switch (sqTipi) {
        case MSSQL -> result = new createMSSQL().dosyaOlustur(serverBilgiDTO);
        case MYSQL -> result = new createMYSQL().dosyaOlustur(serverBilgiDTO);
        case PGSQL -> result = new createPGSQL().dosyaOlustur(serverBilgiDTO);
    }
		return result;
	}

	public void job_sil_S(String jobName, String dosya,serverBilgiDTO serverBilgiDTO) throws SQLException {

		sqlTipi sqTipi = sqlTipi.fromString(serverBilgiDTO.getHangi_sql());
		switch (sqTipi) {
        case MSSQL -> new createMSSQL().job_sil_S(jobName, serverBilgiDTO);
        case MYSQL -> new createMYSQL().job_sil_S(jobName, serverBilgiDTO);
        case PGSQL -> new createPGSQL().job_sil_S(jobName, serverBilgiDTO);
    }
	}

	public void job_olustur_S(String jobName, String dosya,String indexISIM , serverBilgiDTO serverBilgiDTO) throws SQLException {
		sqlTipi sqTipi = sqlTipi.fromString(serverBilgiDTO.getHangi_sql());
		switch (sqTipi) {
		case MSSQL -> new createMSSQL().job_olustur_S(jobName, dosya, indexISIM, serverBilgiDTO);
		case MYSQL -> new createMYSQL().job_olustur_S(jobName, dosya, indexISIM, serverBilgiDTO);
		case PGSQL -> new createPGSQL().job_olustur_S(jobName, dosya, indexISIM, serverBilgiDTO);
		}
	}

	public void job_baslat_S(String jobName, serverBilgiDTO serverBilgiDTO) throws SQLException {
		sqlTipi sqTipi = sqlTipi.fromString(serverBilgiDTO.getHangi_sql());
		if(sqTipi.equals(sqlTipi.MSSQL))
		{
			createMSSQL createMSSQL = new createMSSQL();
			createMSSQL.job_baslat_S(jobName,serverBilgiDTO);
		}
	}
}