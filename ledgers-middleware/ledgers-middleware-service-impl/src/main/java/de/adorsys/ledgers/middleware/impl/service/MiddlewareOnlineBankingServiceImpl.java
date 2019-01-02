package de.adorsys.ledgers.middleware.impl.service;

import java.time.LocalDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.payment.LoginKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAMethodNotSupportedMiddleException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserScaDataNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.exception.SCAMethodNotSupportedException;
import de.adorsys.ledgers.sca.exception.SCAOperationExpiredException;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationUsedOrStolenException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.Ids;

@Service
public class MiddlewareOnlineBankingServiceImpl implements MiddlewareOnlineBankingService {
	private static final Logger logger = LoggerFactory.getLogger(MiddlewarePaymentServiceImpl.class);
	private final UserService userService;
	private final UserMapper userTOMapper;
	private final BearerTokenMapper bearerTokenMapper;
	private final SCAOperationService scaOperationService;
	private final SCAUtils scaUtils;

	public MiddlewareOnlineBankingServiceImpl(UserService userService, UserMapper userTOMapper,
			BearerTokenMapper bearerTokenMapper, SCAOperationService scaOperationService, SCAUtils scaUtils) {
		super();
		this.userService = userService;
		this.userTOMapper = userTOMapper;
		this.bearerTokenMapper = bearerTokenMapper;
		this.scaOperationService = scaOperationService;
		this.scaUtils = scaUtils;
	}

	@Override
	@SuppressWarnings({"PMD.IdenticalCatchBranches", "PMD.CyclomaticComplexity"})
	public SCALoginResponseTO authorise(String login, String pin, UserRoleTO role)
			throws UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException {
		try {
			UserRoleBO roleBo = UserRoleBO.valueOf(role.name());
			BearerTokenTO loginToken = bearerTokenMapper.toBearerTokenTO(userService.authorise(login, pin, roleBo));
			if(loginToken!=null) {
				UserBO user = userService.findByLogin(login);
				LoginKeyDataTO keyData = new LoginKeyDataTO(loginToken.getAccessTokenObject().getSub(), LocalDateTime.now());
				SCALoginResponseTO response = new SCALoginResponseTO();
				if(!scaRequired(user, OpTypeBO.LOGIN)) {
					response.setScaStatus(ScaStatusTO.EXEMPTED);		
					String scaId = Ids.id();
					BearerTokenBO scaTokenBO = userService.scaToken(user.getId(), scaId, 1800, UserRoleBO.valueOf(role.name()));
					BearerTokenTO scaTokenTO = bearerTokenMapper.toBearerTokenTO(scaTokenBO);
					response.setBearerToken(scaTokenTO);
					response.setScaId(scaId);
					response.setExpiresInSeconds(1800);
					response.setStatusDate(LocalDateTime.now());
				} else {
					SCAOperationBO scaOperationBO;
					UserTO userTo = scaUtils.user(user);
					AuthCodeDataBO authCodeData = authCodeData(keyData, login, OpTypeBO.LOGIN);
					if (userTo.getScaUserData().size() == 1) {
						ScaUserDataTO chosenScaMethod = userTo.getScaUserData().iterator().next();
						authCodeData.setScaUserDataId(chosenScaMethod.getId());
						try {
							scaOperationBO = scaOperationService.generateAuthCode(authCodeData, user, ScaStatusBO.SCAMETHODSELECTED);
						} catch (SCAMethodNotSupportedException | UserScaDataNotFoundException | SCAOperationValidationException
								| SCAOperationNotFoundException e) {
							throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
						}
					} else {
						scaOperationBO = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.PSUIDENTIFIED);
					}
					response = toScaResponse(user, login, keyData, scaOperationBO);
					response.setBearerToken(loginToken);
				}
				return response;
			}
		} catch (UserNotFoundException e) {
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		}
		throw new InsufficientPermissionMiddlewareException("Unknown credentials.");
	}

	@Override
	public BearerTokenTO validate(String accessToken) throws UserNotFoundMiddlewareException {
		try {
			return bearerTokenMapper.toBearerTokenTO(userService.validate(accessToken, new Date()));
		} catch (UserNotFoundException e) {
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO register(String login, String email, String pin, UserRoleTO role)
			throws UserAlreadyExistsMiddlewareException {
		UserTO user = new UserTO(login, email, pin);
		user.getUserRoles().add(role);
		UserBO userBO = userTOMapper.toUserBO(user);
		try {
			return userTOMapper.toUserTO(userService.create(userBO));
		} catch (UserAlreadyExistsException e) {
			throw new UserAlreadyExistsMiddlewareException(user, e);
		}
	}

	@Override
	@SuppressWarnings({"PMD.IdenticalCatchBranches", "PMD.CyclomaticComplexity"})
	public SCALoginResponseTO generateLoginAuthCode(String scaUserDataId, String authorisationId, String userMessage,
			int validitySeconds) throws SCAOperationNotFoundMiddlewareException, InsufficientPermissionMiddlewareException, 
			SCAMethodNotSupportedMiddleException, UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException{
		try {
			UserBO user = scaUtils.userBO();
			SCAOperationBO scaOperationBO = scaOperationService.loadAuthCode(authorisationId);
			LoginKeyDataTO keyData = LoginKeyDataTO.fromOpId(scaOperationBO.getOpId());
			AuthCodeDataBO authCodeData = authCodeData(keyData, user.getLogin(), OpTypeBO.LOGIN);
			scaOperationService.generateAuthCode(authCodeData, user, ScaStatusBO.SCAMETHODSELECTED);
			SCALoginResponseTO scaResponse = toScaResponse(user, authorisationId, keyData, scaOperationBO);
			BearerTokenTO scaToken = bearerTokenMapper.toBearerTokenTO(userService.scaToken(user.getId(),authorisationId, validitySeconds, UserRoleBO.valueOf(scaUtils.getCurrentRole().name())));
			scaResponse.setBearerToken(scaToken);
			return scaResponse;
		} catch (SCAMethodNotSupportedException e) {
			throw new SCAMethodNotSupportedMiddleException(e);
		} catch (UserScaDataNotFoundException e) {
			throw new UserScaDataNotFoundMiddlewareException(e);
		} catch (SCAOperationNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new SCAOperationNotFoundMiddlewareException(e);
		} catch (SCAOperationValidationException e) {
			logger.error(e.getMessage(), e);
			throw new SCAOperationValidationMiddlewareException(e);
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	@SuppressWarnings({"PMD.IdenticalCatchBranches", "PMD.CyclomaticComplexity"})
	public SCALoginResponseTO authenticateForLogin(String authorisationId, String authCode)
			throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, 
			InsufficientPermissionMiddlewareException {
		try {
			UserBO user = scaUtils.userBO();
			SCAOperationBO scaOperationBO = scaOperationService.loadAuthCode(authorisationId);
			LoginKeyDataTO keyData = LoginKeyDataTO.fromOpId(scaOperationBO.getOpId());
			boolean valid = scaOperationService.validateAuthCode(authorisationId, authorisationId, authorisationId, authCode);
			SCALoginResponseTO scaResponse = toScaResponse(user, authorisationId, keyData, scaOperationBO);
			if(valid) {
				BearerTokenTO scaToken = bearerTokenMapper.toBearerTokenTO(userService.scaToken(user.getId(),authorisationId, 1800, UserRoleBO.valueOf(scaUtils.getCurrentRole().name())));
				scaResponse.setBearerToken(scaToken);
			}
			return scaResponse;
		} catch (SCAOperationNotFoundException e) {
			throw new SCAOperationNotFoundMiddlewareException(e);
		} catch (SCAOperationValidationException e) {
			throw new SCAOperationValidationMiddlewareException(e);
		} catch (SCAOperationExpiredException e) {
			throw new SCAOperationExpiredMiddlewareException(e);
		} catch (SCAOperationUsedOrStolenException e) {
			throw new SCAOperationUsedOrStolenMiddlewareException(e);
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		}
	}

	private SCALoginResponseTO toScaResponse(UserBO user, String scaId, LoginKeyDataTO keyData,
			SCAOperationBO a) {
		SCALoginResponseTO response = new SCALoginResponseTO();
		UserTO userTO = scaUtils.user(user);
		response.setAuthorisationId(a.getId());
		response.setChosenScaMethod(scaUtils.getScaMethod(userTO, a.getScaMethodId()));
		response.setChallengeData(null);
		response.setExpiresInSeconds(a.getValiditySeconds());
		response.setScaId(scaId);
		response.setPsuMessage(keyData.messageTemplate());
		response.setScaMethods(userTO.getScaUserData());
		response.setScaStatus(ScaStatusTO.valueOf(a.getScaStatus().name()));
		response.setStatusDate(a.getStatusTime());
		return response;
	}

	private AuthCodeDataBO authCodeData(LoginKeyDataTO keyData, String login, OpTypeBO opType) {
		// Will be also used as opid and authorization id
		String opData = keyData.toOpId();
		AuthCodeDataBO authCodeData = new AuthCodeDataBO();
		authCodeData.setOpData(opData);
		authCodeData.setOpId(opData);
		authCodeData.setAuthorisationId(opData);
		authCodeData.setOpType(opType);
		authCodeData.setUserLogin(login);
		authCodeData.setUserMessage(keyData.messageTemplate());
		authCodeData.setValiditySeconds(1800);
		return authCodeData;
	}
	@SuppressWarnings("PMD.UnusedFormalParameter")
	private boolean scaRequired(UserBO user, OpTypeBO opType) {
		return scaUtils.hasSCA(user);
	}
	
}
