package com.welab.wefe.manager.service.service;

import com.welab.wefe.manager.service.entity.DataSetDefaultTag;
import com.welab.wefe.manager.service.repository.DataSetDefaultTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataSetDefaultTagService {
    @Autowired
    private DataSetDefaultTagRepository dataSetDefaultTagRepository;

    public void save(DataSetDefaultTag dataSetDefaultTag) {
        dataSetDefaultTagRepository.save(dataSetDefaultTag);
    }

    public void deleteById(String id) {
        dataSetDefaultTagRepository.deleteById(id);
    }

    public List<DataSetDefaultTag> findAll() {
        return dataSetDefaultTagRepository.findAll();
    }


    public DataSetDefaultTag findById(String id) {
        Optional<DataSetDefaultTag> optional = dataSetDefaultTagRepository.findById(id);
        return optional.orElse(null);
    }
}
