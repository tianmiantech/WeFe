//package com.welab.wefe.serving.service.api.service;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.welab.wefe.common.exception.StatusCodeWithException;
//import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
//import com.welab.wefe.common.web.api.base.Api;
//import com.welab.wefe.common.web.dto.ApiResult;
//import com.welab.wefe.common.web.util.ModelMapper;
//import com.welab.wefe.serving.service.database.entity.ModelMySqlModel;
//import com.welab.wefe.serving.service.database.entity.ServiceMySqlModel;
//import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
//import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
//import com.welab.wefe.serving.service.database.repository.ModelRepository;
//import com.welab.wefe.serving.service.database.repository.ServiceRepository;
//import com.welab.wefe.serving.service.database.repository.TableModelRepository;
//import com.welab.wefe.serving.service.database.repository.TableServiceRepository;
//
//@Api(path = "service/init", name = "init service", login = false)
//public class InitApi extends AbstractNoneInputApi<InitApi.Output> {
//
//    @Autowired
//    private ModelRepository modelRepo;
//
//    @Autowired
//    private ServiceRepository serviceRepo;
//
//    @Autowired
//    private TableModelRepository tableModelRepo;
//
//    @Autowired
//    private TableServiceRepository tableServiceRepo;
//
//    public static class Output {
//
//    }
//
//    @Override
//    protected ApiResult<Output> handle() throws StatusCodeWithException {
//        tableServiceRepo.deleteAll();
//        tableModelRepo.deleteAll();
//        
//        List<ServiceMySqlModel> services = serviceRepo.findAll();
//
//        for (ServiceMySqlModel service : services) {
//            TableServiceMySqlModel t = ModelMapper.map(service, TableServiceMySqlModel.class);
//            tableServiceRepo.save(t);
//        }
//
//        List<ModelMySqlModel> models = modelRepo.findAll();
//        for (ModelMySqlModel model : models) {
//            TableModelMySqlModel t = ModelMapper.map(model, TableModelMySqlModel.class);
//            t.setServiceId(model.getModelId());
//            tableModelRepo.save(t);
//        }
//        return success();
//
//    }
//}
