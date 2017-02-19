#import <Foundation/Foundation.h>

#import "sqlite3.h"

@interface Alumno : NSObject{
	NSInteger myid;
	NSString * nombre;
	NSString * dni;
	double nota;
	NSInteger chico;
}

	@property (nonatomic) NSInteger myid;
	@property (nonatomic) NSString * nombre;
	@property (nonatomic) NSString * dni;
	@property (nonatomic) double nota;
	@property (nonatomic) NSInteger chico;
@end